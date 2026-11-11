
package com.vortex.smp

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.zip.ZipInputStream

object ZipImporter {

    fun importZip(context: Context, uri: Uri): File {
        val destination = File(context.filesDir, "server")

        if (destination.exists()) {
            destination.deleteRecursively()
        }

        if (!destination.mkdirs()) {
            throw Exception("Não foi possível criar a pasta do servidor.")
        }

        var entries = 0
        var totalBytes = 0L

        val input = context.contentResolver.openInputStream(uri)
            ?: throw Exception("Não foi possível abrir o ZIP.")

        input.use { source ->
            ZipInputStream(source.buffered()).use { zip ->

                var entry = zip.nextEntry

                while (entry != null) {
                    entries++

                    if (entries > 10000) {
                        throw Exception("ZIP possui arquivos demais.")
                    }

                    val outputFile = File(destination, entry.name)
                    val safePath = destination.canonicalPath +
                        File.separator

                    if (!outputFile.canonicalPath.startsWith(safePath)) {
                        throw Exception("Caminho inválido no ZIP.")
                    }

                    if (entry.isDirectory) {
                        outputFile.mkdirs()
                    } else {
                        outputFile.parentFile?.mkdirs()

                        outputFile.outputStream().buffered().use { output ->
                            val buffer = ByteArray(8192)

                            while (true) {
                                val count = zip.read(buffer)
                                if (count == -1) break

                                totalBytes += count

                                if (totalBytes > 2L * 1024 * 1024 * 1024) {
                                    throw Exception("ZIP maior que 2 GB extraídos.")
                                }

                                output.write(buffer, 0, count)
                            }
                        }
                    }

                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }
        }

        if (entries == 0) {
            throw Exception("O ZIP está vazio.")
        }

        return destination
    }
}
