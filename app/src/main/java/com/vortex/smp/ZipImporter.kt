
package com.vortex.smp

import android.content.Context
import android.net.Uri
import java.util.zip.ZipFile
import java.io.File
import java.io.FileOutputStream

object ZipImporter {

    fun importZip(
        context: Context,
        uri: Uri,
        destination: File
    ): String {
        val input = context.contentResolver.openInputStream(uri)
            ?: return "Não foi possível abrir o ZIP."

        val zipFile = File(context.cacheDir, "vortex-import.zip")

        input.use { source ->
            FileOutputStream(zipFile).use { output ->
                source.copyTo(output)
            }
        }

        var count = 0
        val maxFiles = 10000
        val maxBytes = 2L * 1024 * 1024 * 1024

        ZipFile(zipFile).use { zip ->
            val entries = zip.entries()

            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                val name = entry.name.replace('\\', '/')
                val target = File(destination, name).canonicalFile

                require(
                    target.path.startsWith(
                        destination.canonicalPath + File.separator
                    ) || target == destination.canonicalFile
                ) { "Caminho inválido no ZIP." }

                count++
                require(count <= maxFiles) { "ZIP possui arquivos demais." }

                if (entry.isDirectory) {
                    target.mkdirs()
                } else {
                    target.parentFile?.mkdirs()

                    zip.getInputStream(entry).use { source ->
                        FileOutputStream(target).use { output ->
                            val buffer = ByteArray(8192)
                            var total = 0L
                            while (true) {
                                val read = source.read(buffer)
                                if (read == -1) break
                                total += read
                                require(total <= maxBytes) {
                                    "Arquivo muito grande."
                                }
                                output.write(buffer, 0, read)
                            }
                        }
                    }
                }
            }
        }

        return "Importação concluída: $count itens."
    }
}
