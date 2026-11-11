
package com.vortex.smp

import java.io.File

object ServerManager {

    // Procura arquivos e pastas em todas as subpastas.
    private fun allFiles(root: File): Sequence<File> {
        if (!root.exists() || !root.isDirectory) {
            return emptySequence()
        }

        return root.walkTopDown()
            .maxDepth(15)
            .filter { it.exists() }
    }

    // Encontra a pasta principal do servidor.
    fun findServerRoot(importDir: File): File {
        val folders = allFiles(importDir)
            .filter { it.isDirectory }
            .toList()

        return folders.firstOrNull { folder ->
            File(folder, "plugins").isDirectory &&
            (
                File(folder, "server.properties").exists() ||
                File(folder, "bukkit.yml").exists() ||
                File(folder, "paper.yml").exists() ||
                File(folder, "purpur.yml").exists()
            )
        } ?: importDir
    }

    // Encontra plugins .jar dentro de pastas chamadas plugins.
    fun findPlugins(root: File): List<File> {
        return allFiles(root)
            .filter {
                it.isFile &&
                it.extension.equals("jar", ignoreCase = true) &&
                it.parentFile?.name.equals("plugins", ignoreCase = true)
            }
            .distinctBy { it.absolutePath }
            .toList()
    }

    // Encontra mundos Java pelo arquivo level.dat.
    fun findWorlds(root: File): List<File> {
        return allFiles(root)
            .filter {
                it.isFile &&
                it.name.equals("level.dat", ignoreCase = true)
            }
            .mapNotNull { it.parentFile }
            .distinctBy { it.absolutePath }
            .toList()
    }

    // Procura os arquivos executáveis do servidor.
    fun findServerJars(root: File): List<File> {
        val serverNames = setOf(
            "server.jar",
            "paper.jar",
            "spigot.jar",
            "purpur.jar"
        )

        return allFiles(root)
            .filter {
                it.isFile &&
                it.extension.equals("jar", ignoreCase = true) &&
                it.name.lowercase() in serverNames
            }
            .toList()
    }

    fun hasServerJar(root: File): Boolean {
        return findServerJars(root).isNotEmpty()
    }

    fun memoryArgs(): List<String> {
        return listOf("-Xms1G", "-Xmx4G")
    }
}
