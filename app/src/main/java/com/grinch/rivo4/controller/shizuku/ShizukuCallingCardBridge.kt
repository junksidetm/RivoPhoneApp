package com.grinch.rivo4.controller.shizuku

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.ContactsContract
import com.grinch.rivo4.controller.util.CallBackgroundStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.yield
import java.io.File
import java.io.FileInputStream

object ShizukuCallingCardBridge {

    const val GOOGLE_DIALER_PACKAGE = "com.google.android.dialer"
    const val GOOGLE_CONTACTS_PACKAGE = "com.google.android.contacts"
    const val CONTACTS_PROVIDER_PACKAGE = "com.android.providers.contacts"

    data class BridgeResult(
        val success: Boolean,
        val message: String,
        val imageUri: String? = null
    )

    data class BatchSyncResult(
        val totalContacts: Int,
        val syncedCount: Int,
        val skippedCount: Int,
        val errorCount: Int
    )

    fun isShizukuInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo("moe.shizuku.privileged.api", 0) != null
        } catch (_: Exception) {
            false
        }
    }

    fun isShizukuAvailable(): Boolean {
        return ShizukuConnectionManager.isAvailable()
    }

    fun hasShizukuPermission(context: Context? = null): Boolean {
        return ShizukuConnectionManager.hasPermission(context)
    }

    fun requestShizukuPermission() {
        ShizukuConnectionManager.requestPermission()
    }

    /**
     * Attempts to extract a Calling Card or high-res Contact Poster for a specific contact.
     * Tier 1: Privileged extraction from Google Phone / Contacts sandbox via Shizuku UserService.
     * Tier 2: ContactsContract high-resolution display photo and custom dialer poster data fallback.
     */
    suspend fun syncContactCallingCard(
        context: Context,
        contactId: String?,
        numbers: List<String>,
        contactName: String? = null
    ): BridgeResult = withContext(Dispatchers.IO) {
        val app = context.applicationContext
        val cleanNumbers = numbers.filter { it.isNotBlank() }
        val rawDigitsList = cleanNumbers.map { it.filter { ch -> ch.isDigit() } }.filter { it.isNotEmpty() }

        // Tier 1: Privileged extraction via Shizuku UserService
        if (isShizukuAvailable() && hasShizukuPermission(app)) {
            val shizukuResult = try {
                withTimeoutOrNull(4000L) {
                    extractViaShizuku(app, contactId, rawDigitsList)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
            if (shizukuResult != null) {
                val saved = CallBackgroundStore.saveBitmap(app, contactId, cleanNumbers, shizukuResult)
                shizukuResult.recycle()
                if (saved) {
                    val peekedUri = CallBackgroundStore.peek(app, contactId, cleanNumbers)
                    return@withContext BridgeResult(
                        success = true,
                        message = "Calling Card synced from Google Phone!",
                        imageUri = peekedUri
                    )
                }
            }
        }

        // Tier 2: ContactsContract Display Photo & Contact Poster fallback
        val highResBitmap = try {
            extractFromContactsContract(app, contactId, cleanNumbers)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
        if (highResBitmap != null) {
            val saved = CallBackgroundStore.saveBitmap(app, contactId, cleanNumbers, highResBitmap)
            highResBitmap.recycle()
            if (saved) {
                val peekedUri = CallBackgroundStore.peek(app, contactId, cleanNumbers)
                return@withContext BridgeResult(
                    success = true,
                    message = "High-resolution contact poster synced!",
                    imageUri = peekedUri
                )
            }
        }

        val reason = if (!isShizukuAvailable()) {
            "Shizuku is not running. Start Shizuku to sync Google Phone Calling Cards."
        } else if (!hasShizukuPermission(app)) {
            "Shizuku permission not granted. Grant permission to access Google Phone Calling Cards."
        } else {
            "No Calling Card or poster found for this contact."
        }

        BridgeResult(success = false, message = reason)
    }

    private suspend fun extractViaShizuku(
        context: Context,
        contactId: String?,
        rawDigitsList: List<String>
    ): Bitmap? = withTimeoutOrNull(4000L) {
        val manager = ShizukuConnectionManager(context)
        try {
            val service = manager.getShellService()

            // Search candidate paths in Google Phone and Contacts sandbox (exclude /sdcard to prevent FUSE deadlocks)
            val searchDirs = listOf(
                "/data/data/$GOOGLE_DIALER_PACKAGE/files/calling_cards",
                "/data/data/$GOOGLE_DIALER_PACKAGE/files/call_cards",
                "/data/data/$GOOGLE_DIALER_PACKAGE/files/posters",
                "/data/data/$GOOGLE_DIALER_PACKAGE/files/photos",
                "/data/data/$GOOGLE_CONTACTS_PACKAGE/files/calling_cards",
                "/data/data/$GOOGLE_CONTACTS_PACKAGE/files/posters",
                "/data/data/$CONTACTS_PROVIDER_PACKAGE/files/photos"
            )

            // Fast find with -maxdepth 2 to avoid deep traversals
            val findCmd = "find ${searchDirs.joinToString(" ")} -maxdepth 2 -type f \\( -name \"*.jpg\" -o -name \"*.jpeg\" -o -name \"*.png\" -o -name \"*.webp\" \\) 2>/dev/null"
            val output = service.execCommand(findCmd)?.trim().orEmpty()

            if (output.isNotBlank()) {
                val filePaths = output.lines().map { it.trim() }.filter { it.isNotEmpty() }
                val bestPath = matchBestCallingCardPath(filePaths, contactId, rawDigitsList)
                if (bestPath != null) {
                    val pfd = service.readFile(bestPath)
                    if (pfd != null) {
                        try {
                            pfd.use {
                                val fd = it.fileDescriptor
                                return@withTimeoutOrNull BitmapFactory.decodeFileDescriptor(fd)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            manager.unbind()
        }
    }

    private fun matchBestCallingCardPath(
        filePaths: List<String>,
        contactId: String?,
        rawDigitsList: List<String>
    ): String? {
        if (filePaths.isEmpty()) return null

        // 1. Match by contactId if available
        if (!contactId.isNullOrBlank()) {
            filePaths.firstOrNull { it.contains(contactId) }?.let { return it }
        }

        // 2. Match by phone digits (last 7-10 digits)
        for (digits in rawDigitsList) {
            val key = if (digits.length > 7) digits.takeLast(7) else digits
            if (key.length >= 4) {
                filePaths.firstOrNull { it.contains(key) }?.let { return it }
            }
        }

        // 3. Fallback: if in calling_cards directory, return the most relevant or recent
        val callingCardFiles = filePaths.filter { it.contains("calling_card") || it.contains("poster") }
        if (callingCardFiles.isNotEmpty()) {
            return callingCardFiles.first()
        }

        return null
    }

    private fun extractFromContactsContract(
        context: Context,
        contactId: String?,
        numbers: List<String>
    ): Bitmap? {
        val cr = context.contentResolver

        // Try direct contactId
        if (!contactId.isNullOrBlank()) {
            val contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId.toLongOrNull() ?: return null)
            // 1. High-res display photo
            try {
                val displayPhotoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.DISPLAY_PHOTO)
                cr.openAssetFileDescriptor(displayPhotoUri, "r")?.use { afd ->
                    afd.createInputStream().use { stream ->
                        val bmp = BitmapFactory.decodeStream(stream)
                        if (bmp != null) return bmp
                    }
                }
            } catch (_: Exception) {
            }

            // 2. Standard photo input stream with preferHighres = true
            try {
                ContactsContract.Contacts.openContactPhotoInputStream(cr, contactUri, true)?.use { stream ->
                    val bmp = BitmapFactory.decodeStream(stream)
                    if (bmp != null) return bmp
                }
            } catch (_: Exception) {
            }
        }

        // Try phone lookup for contact ID
        for (number in numbers) {
            val lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number))
            var resolvedId: Long? = null
            try {
                cr.query(lookupUri, arrayOf(ContactsContract.PhoneLookup._ID), null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        resolvedId = cursor.getLong(0)
                    }
                }
            } catch (_: Exception) {
            }

            if (resolvedId != null) {
                val contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, resolvedId!!)
                try {
                    val displayPhotoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.DISPLAY_PHOTO)
                    cr.openAssetFileDescriptor(displayPhotoUri, "r")?.use { afd ->
                        afd.createInputStream().use { stream ->
                            val bmp = BitmapFactory.decodeStream(stream)
                            if (bmp != null) return bmp
                        }
                    }
                } catch (_: Exception) {
                }

                try {
                    ContactsContract.Contacts.openContactPhotoInputStream(cr, contactUri, true)?.use { stream ->
                        val bmp = BitmapFactory.decodeStream(stream)
                        if (bmp != null) return bmp
                    }
                } catch (_: Exception) {
                }
            }
        }

        return null
    }

    /**
     * Batch sync Calling Cards and contact posters across all contacts.
     */
    suspend fun syncAllCallingCards(
        context: Context,
        overwriteExisting: Boolean = false,
        onProgress: (current: Int, total: Int, name: String) -> Unit = { _, _, _ -> }
    ): BatchSyncResult = withContext(Dispatchers.IO) {
        val app = context.applicationContext
        val cr = app.contentResolver

        data class ContactRecord(val id: String, val name: String, val numbers: MutableList<String>)
        val contactsMap = mutableMapOf<String, ContactRecord>()

        try {
            cr.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                ),
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )?.use { cursor ->
                val idIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                while (cursor.moveToNext()) {
                    val id = cursor.getString(idIdx) ?: continue
                    val name = cursor.getString(nameIdx) ?: "Unknown"
                    val number = cursor.getString(numIdx) ?: continue
                    val record = contactsMap.getOrPut(id) { ContactRecord(id, name, mutableListOf()) }
                    record.numbers.add(number)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val allRecords = contactsMap.values.toList()
        val total = allRecords.size
        var synced = 0
        var skipped = 0
        var errors = 0

        var shizukuManager: ShizukuConnectionManager? = null
        var candidateFiles = emptyList<String>()

        if (isShizukuAvailable() && hasShizukuPermission(app)) {
            try {
                val mgr = ShizukuConnectionManager(app)
                val service = withTimeoutOrNull(3500L) { mgr.getShellService() }
                if (service != null) {
                    shizukuManager = mgr
                    val searchDirs = listOf(
                        "/data/data/$GOOGLE_DIALER_PACKAGE/files/calling_cards",
                        "/data/data/$GOOGLE_DIALER_PACKAGE/files/call_cards",
                        "/data/data/$GOOGLE_DIALER_PACKAGE/files/posters",
                        "/data/data/$GOOGLE_DIALER_PACKAGE/files/photos",
                        "/data/data/$GOOGLE_CONTACTS_PACKAGE/files/calling_cards",
                        "/data/data/$GOOGLE_CONTACTS_PACKAGE/files/posters",
                        "/data/data/$CONTACTS_PROVIDER_PACKAGE/files/photos"
                    )
                    val findCmd = "find ${searchDirs.joinToString(" ")} -maxdepth 2 -type f \\( -name \"*.jpg\" -o -name \"*.jpeg\" -o -name \"*.png\" -o -name \"*.webp\" \\) 2>/dev/null"
                    val out = service.execCommand(findCmd)?.trim().orEmpty()
                    if (out.isNotBlank()) {
                        candidateFiles = out.lines().map { it.trim() }.filter { it.isNotEmpty() }
                    }
                } else {
                    mgr.unbind()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        try {
            allRecords.forEachIndexed { index, record ->
                yield()
                onProgress(index + 1, total, record.name)
                val cleanNumbers = record.numbers.filter { it.isNotBlank() }
                val existing = CallBackgroundStore.peek(app, record.id, cleanNumbers)
                if (existing != null && !overwriteExisting) {
                    skipped++
                    return@forEachIndexed
                }

                var imported = false
                val rawDigits = cleanNumbers.map { it.filter { ch -> ch.isDigit() } }.filter { it.isNotEmpty() }

                val mgr = shizukuManager
                if (mgr != null && candidateFiles.isNotEmpty()) {
                    val bestPath = matchBestCallingCardPath(candidateFiles, record.id, rawDigits)
                    if (bestPath != null) {
                        try {
                            val service = mgr.getShellService()
                            val pfd = service.readFile(bestPath)
                            if (pfd != null) {
                                val bmp = pfd.use {
                                    BitmapFactory.decodeFileDescriptor(it.fileDescriptor)
                                }
                                if (bmp != null) {
                                    val saved = CallBackgroundStore.saveBitmap(app, record.id, cleanNumbers, bmp)
                                    bmp.recycle()
                                    if (saved) {
                                        synced++
                                        imported = true
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                if (!imported) {
                    val fallbackBmp = try {
                        extractFromContactsContract(app, record.id, cleanNumbers)
                    } catch (e: Exception) {
                        null
                    }
                    if (fallbackBmp != null) {
                        val saved = CallBackgroundStore.saveBitmap(app, record.id, cleanNumbers, fallbackBmp)
                        fallbackBmp.recycle()
                        if (saved) {
                            synced++
                            imported = true
                        }
                    }
                }

                if (!imported) {
                    skipped++
                }
            }
        } finally {
            shizukuManager?.unbind()
        }

        BatchSyncResult(
            totalContacts = total,
            syncedCount = synced,
            skippedCount = skipped,
            errorCount = errors
        )
    }
}
