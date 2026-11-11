
package com.vortex.smp

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.IOException
import java.util.zip.ZipInputStream

object ZipImporter {

    fun importZip(context: Context, uri: Uri): File {

        val destination = File(context.filesDir, "server")

        if (destination.exists()) {
            if (!destination.deleteRecursively()) {
                throw IOException(
                    "Não foi possível limpar a pasta anterior."
                )
            }
        }

        if (!destination.mkdirs()) {
            throw IOException(
                "Não foi possível criar a pasta do servidor."
            )
        }

        var entries = 0

        val input = context.contentResolver.openInputStream(uri)
            ?: throw IOException(
                "Não foi possível abrir o arquivo ZIP."
            )

        input.use { source ->

            ZipInputStream(source.buffered()).use { zip ->

                var entry = zip.nextEntry

                while (entry != null) {

                    entries++

                    if (entries > 10000) {
                        throw IOException(
                            "O ZIP possui arquivos demais."
                        )
                    }

                    val outputFile = File(destination, entry.name)

                    val safePath = destination.canonicalPath +
                        File.separator

                    if (!outputFile.canonicalPath.startsWith(safePath)) {
                        throw IOException(
                            "Caminho inválido encontrado no ZIP."
                        )
                    }

                    if (entry.isDirectory) {

                        if (!outputFile.exists() &&
                            !outputFile.mkdirs()
                        ) {
                            throw IOException(
                                "Não foi possível criar uma pasta."
                            )
                        }

                    } else {

                        val parent = outputFile.parentFile

                        if (parent != null && !parent.exists()) {
                            if (!parent.mkdirs()) {
                                throw IOException(
                                    "Não foi possível criar uma pasta."
                                )
                            }
                        }

                        outputFile.outputStream().buffered().use { output ->

                            val buffer = ByteArray(8192)

                            while (true) {

                                val count = zip.read(buffer)

                                if (count == -1) break

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
            throw IOException(
                "O arquivo ZIP está vazio ou inválido."
            )
        }

        return destination
    }
}
