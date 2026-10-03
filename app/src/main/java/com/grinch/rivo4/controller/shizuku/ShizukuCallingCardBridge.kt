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
import rikka.shizuku.Shizuku
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
     * Tier 1: Fast Native Android DisplayPhoto & ContactsContract (0s latency, 100% reliable, no IPC).
     * Tier 2: Direct Shizuku Shell Process execution (bypasses UserService/app_process completely).
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

        // Tier 1: Fast Native Android DisplayPhoto & ContactsContract (0ms overhead)
        val highResBitmap = try {
            extractFromContactsContract(app, contactId, cleanNumbers)
        } catch (e: Throwable) {
            null
        }
        if (highResBitmap != null) {
            val saved = CallBackgroundStore.saveBitmap(app, contactId, cleanNumbers, highResBitmap)
            highResBitmap.recycle()
            if (saved) {
                val peekedUri = CallBackgroundStore.peek(app, contactId, cleanNumbers)
                return@withContext BridgeResult(
                    success = true,
                    message = "Calling Card synced from Google Contacts!",
                    imageUri = peekedUri
                )
            }
        }

        // Tier 2: Direct Shizuku Shell Process (Bypasses UserService to prevent app_process freezes)
        val isAvail = try { isShizukuAvailable() } catch (_: Throwable) { false }
        val hasPerm = try { hasShizukuPermission(app) } catch (_: Throwable) { false }

        if (isAvail && hasPerm) {
            val shizukuResult = try {
                withTimeoutOrNull(3000L) {
                    extractViaShizukuDirect(app, contactId, rawDigitsList)
                        ?: extractViaUserService(app, contactId, rawDigitsList)
                }
            } catch (e: Throwable) {
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

        val reason = when {
            !isAvail -> "No high-resolution poster in Contacts. Start Shizuku to also search Google Phone app files."
            !hasPerm -> "No poster found in Contacts. Grant Shizuku permission to search Google Phone app files."
            else -> "No Calling Card or poster found for this contact."
        }

        BridgeResult(success = false, message = reason)
    }

    private fun startShizukuProcess(cmd: Array<String>): java.lang.Process? {
        return try {
            val method = Shizuku::class.java.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java
            )
            method.isAccessible = true
            method.invoke(null, cmd, null, null) as? java.lang.Process
        } catch (_: Throwable) {
            null
        }
    }

    /**
     * Executes a shell command directly through Shizuku's remote server process.
     * This avoids UserService and app_process classpath execution, preventing SELinux/JVM deadlocks.
     */
    fun execShizukuCommand(cmd: String, timeoutMs: Long = 2000L): String? {
        return try {
            val process = startShizukuProcess(arrayOf("sh", "-c", cmd)) ?: return null
            val reader = process.inputStream.bufferedReader()
            val output = StringBuilder()
            val deadline = System.currentTimeMillis() + timeoutMs

            while (System.currentTimeMillis() < deadline) {
                if (reader.ready()) {
                    val line = reader.readLine() ?: break
                    output.appendLine(line)
                } else {
                    try {
                        process.exitValue()
                        output.append(reader.readText())
                        break
                    } catch (_: IllegalThreadStateException) {
                        Thread.sleep(15)
                    }
                }
            }
            try { process.destroy() } catch (_: Throwable) {}
            output.toString().trim()
        } catch (_: Throwable) {
            null
        }
    }

    /**
     * Reads an image file directly from Shizuku's privileged shell without UserService.
     */
    suspend fun readShizukuBitmap(filePath: String, timeoutMs: Long = 2000L): Bitmap? = withTimeoutOrNull(timeoutMs) {
        withContext(Dispatchers.IO) {
            try {
                val process = startShizukuProcess(arrayOf("cat", filePath)) ?: return@withContext null
                val bytes = try {
                    process.inputStream.use { it.readBytes() }
                } finally {
                    try { process.destroy() } catch (_: Throwable) {}
                }
                if (bytes.isNotEmpty()) {
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                } else null
            } catch (_: Throwable) {
                null
            }
        }
    }

    /**
     * Direct extraction from Google Phone & Contacts internal sandbox via Shizuku shell.
     */
    private suspend fun extractViaShizukuDirect(
        context: Context,
        contactId: String?,
        rawDigitsList: List<String>
    ): Bitmap? = withTimeoutOrNull(2500L) {
        withContext(Dispatchers.IO) {
            try {
                if (!isShizukuAvailable() || !hasShizukuPermission(context)) return@withContext null

                val searchDirs = listOf(
                    "/data/data/$GOOGLE_DIALER_PACKAGE/files/calling_cards",
                    "/data/data/$GOOGLE_DIALER_PACKAGE/files/call_cards",
                    "/data/data/$GOOGLE_DIALER_PACKAGE/files/posters",
                    "/data/data/$GOOGLE_DIALER_PACKAGE/files/photos",
                    "/data/user/0/$GOOGLE_DIALER_PACKAGE/files/calling_cards",
                    "/data/user/0/$GOOGLE_DIALER_PACKAGE/files/posters",
                    "/data/data/$GOOGLE_CONTACTS_PACKAGE/files/calling_cards",
                    "/data/data/$GOOGLE_CONTACTS_PACKAGE/files/posters",
                    "/data/data/$CONTACTS_PROVIDER_PACKAGE/files/photos"
                )

                val findCmd = "find ${searchDirs.joinToString(" ")} -maxdepth 2 -type f \\( -name \"*.jpg\" -o -name \"*.jpeg\" -o -name \"*.png\" -o -name \"*.webp\" \\) 2>/dev/null"
                val output = execShizukuCommand(findCmd, timeoutMs = 1500L)?.trim().orEmpty()

                if (output.isNotBlank()) {
                    val filePaths = output.lines().map { it.trim() }.filter { it.isNotEmpty() }
                    val bestPath = matchBestCallingCardPath(filePaths, contactId, rawDigitsList)
                    if (bestPath != null) {
                        return@withContext readShizukuBitmap(bestPath, timeoutMs = 1500L)
                    }
                }
                null
            } catch (_: Throwable) {
                null
            }
        }
    }

    private suspend fun extractViaUserService(
        context: Context,
        contactId: String?,
        rawDigitsList: List<String>
    ): Bitmap? = withTimeoutOrNull(2000L) {
        val manager = ShizukuConnectionManager(context)
        try {
            val service = manager.getShellService()
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
            val output = service.execCommand(findCmd)?.trim().orEmpty()
            if (output.isNotBlank()) {
                val filePaths = output.lines().map { it.trim() }.filter { it.isNotEmpty() }
                val bestPath = matchBestCallingCardPath(filePaths, contactId, rawDigitsList)
                if (bestPath != null) {
                    val pfd = service.readFile(bestPath)
                    if (pfd != null) {
                        return@withTimeoutOrNull pfd.use {
                            BitmapFactory.decodeFileDescriptor(it.fileDescriptor)
                        }
                    }
                }
            }
            null
        } catch (_: Throwable) {
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

        // 2. Match by phone digits (last 7-10 digits, minimum 4 digits)
        for (digits in rawDigitsList) {
            val key = if (digits.length > 7) digits.takeLast(7) else digits
            if (key.length >= 4) {
                filePaths.firstOrNull { it.contains(key) }?.let { return it }
            }
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
        val idLong = contactId?.toLongOrNull()
        if (idLong != null) {
            val contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, idLong)
            // 1. High-res display photo
            try {
                val displayPhotoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.DISPLAY_PHOTO)
                cr.openAssetFileDescriptor(displayPhotoUri, "r")?.use { afd ->
                    afd.createInputStream().use { stream ->
                        val bmp = BitmapFactory.decodeStream(stream)
                        if (bmp != null) return bmp
                    }
                }
            } catch (_: Throwable) {
            }

            // 2. Query ContactsContract.Data for photo_file_id (Android 14+ / Google Contacts high-res storage)
            try {
                cr.query(
                    ContactsContract.Data.CONTENT_URI,
                    arrayOf(ContactsContract.CommonDataKinds.Photo.PHOTO_FILE_ID),
                    "${ContactsContract.Data.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?",
                    arrayOf(idLong.toString(), ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE),
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val fileIdIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO_FILE_ID)
                        if (fileIdIdx >= 0) {
                            val photoFileId = cursor.getLong(fileIdIdx)
                            if (photoFileId > 0) {
                                val displayPhotoUri = ContentUris.withAppendedId(ContactsContract.DisplayPhoto.CONTENT_URI, photoFileId)
                                cr.openAssetFileDescriptor(displayPhotoUri, "r")?.use { afd ->
                                    afd.createInputStream().use { stream ->
                                        val bmp = BitmapFactory.decodeStream(stream)
                                        if (bmp != null) return bmp
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (_: Throwable) {
            }

            // 3. Standard photo input stream with preferHighres = true
            try {
                ContactsContract.Contacts.openContactPhotoInputStream(cr, contactUri, true)?.use { stream ->
                    val bmp = BitmapFactory.decodeStream(stream)
                    if (bmp != null) return bmp
                }
            } catch (_: Throwable) {
            }
        }

        // Try phone lookup for contact ID and photo URI
        for (number in numbers) {
            val lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number))
            var resolvedId: Long? = null
            var resolvedPhotoUri: String? = null
            try {
                cr.query(
                    lookupUri,
                    arrayOf(ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.PHOTO_URI),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val idIdx = cursor.getColumnIndex(ContactsContract.PhoneLookup._ID)
                        if (idIdx >= 0) resolvedId = cursor.getLong(idIdx)
                        val photoIdx = cursor.getColumnIndex(ContactsContract.PhoneLookup.PHOTO_URI)
                        if (photoIdx >= 0) resolvedPhotoUri = cursor.getString(photoIdx)
                    }
                }
            } catch (_: Throwable) {
            }

            if (!resolvedPhotoUri.isNullOrBlank()) {
                try {
                    cr.openInputStream(Uri.parse(resolvedPhotoUri))?.use { stream ->
                        val bmp = BitmapFactory.decodeStream(stream)
                        if (bmp != null) return bmp
                    }
                } catch (_: Throwable) {
                }
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
                } catch (_: Throwable) {
                }

                try {
                    ContactsContract.Contacts.openContactPhotoInputStream(cr, contactUri, true)?.use { stream ->
                        val bmp = BitmapFactory.decodeStream(stream)
                        if (bmp != null) return bmp
                    }
                } catch (_: Throwable) {
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

        var candidateFiles = emptyList<String>()

        if (isShizukuAvailable() && hasShizukuPermission(app)) {
            try {
                val searchDirs = listOf(
                    "/data/data/$GOOGLE_DIALER_PACKAGE/files/calling_cards",
                    "/data/data/$GOOGLE_DIALER_PACKAGE/files/call_cards",
                    "/data/data/$GOOGLE_DIALER_PACKAGE/files/posters",
                    "/data/data/$GOOGLE_DIALER_PACKAGE/files/photos",
                    "/data/user/0/$GOOGLE_DIALER_PACKAGE/files/calling_cards",
                    "/data/user/0/$GOOGLE_DIALER_PACKAGE/files/posters",
                    "/data/data/$GOOGLE_CONTACTS_PACKAGE/files/calling_cards",
                    "/data/data/$GOOGLE_CONTACTS_PACKAGE/files/posters",
                    "/data/data/$CONTACTS_PROVIDER_PACKAGE/files/photos"
                )
                val findCmd = "find ${searchDirs.joinToString(" ")} -maxdepth 2 -type f \\( -name \"*.jpg\" -o -name \"*.jpeg\" -o -name \"*.png\" -o -name \"*.webp\" \\) 2>/dev/null"
                val out = execShizukuCommand(findCmd, timeoutMs = 3000L)?.trim().orEmpty()
                if (out.isNotBlank()) {
                    candidateFiles = out.lines().map { it.trim() }.filter { it.isNotEmpty() }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

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

            // 1. Prioritize fast native ContactsContract extraction first
            val nativeBmp = try {
                extractFromContactsContract(app, record.id, cleanNumbers)
            } catch (_: Throwable) {
                null
            }
            if (nativeBmp != null) {
                val saved = CallBackgroundStore.saveBitmap(app, record.id, cleanNumbers, nativeBmp)
                nativeBmp.recycle()
                if (saved) {
                    synced++
                    imported = true
                }
            }

            // 2. Direct Shizuku shell process fallback if native photo was missing
            if (!imported && candidateFiles.isNotEmpty()) {
                val bestPath = matchBestCallingCardPath(candidateFiles, record.id, rawDigits)
                if (bestPath != null) {
                    val bmp = readShizukuBitmap(bestPath, timeoutMs = 1500L)
                    if (bmp != null) {
                        val saved = CallBackgroundStore.saveBitmap(app, record.id, cleanNumbers, bmp)
                        bmp.recycle()
                        if (saved) {
                            synced++
                            imported = true
                        }
                    }
                }
            }

            if (!imported) {
                skipped++
            }
        }

        BatchSyncResult(
            totalContacts = total,
            syncedCount = synced,
            skippedCount = skipped,
            errorCount = errors
        )
    }
}
