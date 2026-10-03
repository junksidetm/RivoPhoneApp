package com.grinch.rivo4.controller.shizuku

import android.content.Context
import android.os.ParcelFileDescriptor
import androidx.annotation.Keep
import com.grinch.rivo4.IShellService

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
    }

    override fun execCommand(command: String?): String? {
        if (command.isNullOrBlank()) return null
        return try {
            val process = ProcessBuilder(listOf("sh", "-c", command))
                .redirectErrorStream(true)
                .start()
            val output = StringBuilder()
            val reader = process.inputStream.bufferedReader()
            val readerThread = Thread {
                try {
                    reader.lineSequence().forEach { line ->
                        if (output.length < 512 * 1024) {
                            output.append(line).append('\n')
                        }
                    }
                } catch (_: Exception) {
                }
            }
            readerThread.start()
            val finished = process.waitFor(4, java.util.concurrent.TimeUnit.SECONDS)
            if (!finished) {
                process.destroyForcibly()
            }
            readerThread.join(1000)
            output.toString().trim()
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
                    var proc: Process? = null
                    try {
                        proc = ProcessBuilder(listOf("sh", "-c", "cat \"$path\""))
                            .redirectErrorStream(false)
                            .start()
                        ParcelFileDescriptor.AutoCloseOutputStream(writeEnd).use { out ->
                            proc.inputStream.copyTo(out)
                            out.flush()
                        }
                        proc.waitFor(3, java.util.concurrent.TimeUnit.SECONDS)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        proc?.destroyForcibly()
                        try {
                            writeEnd.close()
                        } catch (_: Exception) {
                        }
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
