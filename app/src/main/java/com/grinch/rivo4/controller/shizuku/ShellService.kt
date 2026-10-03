package com.grinch.rivo4.controller.shizuku

import android.content.Context
import android.os.ParcelFileDescriptor
import androidx.annotation.Keep
import com.grinch.rivo4.IShellService
import kotlin.system.exitProcess

@Keep
class ShellService : IShellService.Stub {

    private val pipeline by lazy { ShellAudioPipeline() }

    @Keep
    constructor() : this(null)

    @Keep
    constructor(context: Context?)

    override fun startCapture(
        audioSource: String?,
        audioCodec: String?,
        audioBitRate: Int,
        serverPath: String?,
        debug: Boolean
    ): ParcelFileDescriptor? {
        val source = audioSource ?: "voice-call"
        val codec = audioCodec ?: "aac"
        val path = serverPath ?: return null

        return pipeline.startCapture(
            audioSource = source,
            audioCodec = codec,
            audioBitRate = audioBitRate,
            serverPath = path,
            debug = debug
        )
    }

    override fun stopCapture() {
        pipeline.stopCapture()
    }

    override fun destroy() {
        stopCapture()
        exitProcess(0)
    }

    override fun execCommand(command: String?): String? {
        if (command.isNullOrBlank()) return null
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            val output = process.inputStream.bufferedReader().use { it.readText() }
            process.waitFor()
            output
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun readFile(path: String?): ParcelFileDescriptor? {
        if (path.isNullOrBlank()) return null
        return try {
            val file = java.io.File(path)
            if (file.exists() && file.canRead()) {
                ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            } else {
                val pipe = ParcelFileDescriptor.createPipe()
                val readEnd = pipe[0]
                val writeEnd = pipe[1]
                Thread {
                    try {
                        val proc = Runtime.getRuntime().exec(arrayOf("sh", "-c", "cat \"$path\""))
                        ParcelFileDescriptor.AutoCloseOutputStream(writeEnd).use { out ->
                            proc.inputStream.copyTo(out)
                            out.flush()
                        }
                        proc.waitFor()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }.start()
                readEnd
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
