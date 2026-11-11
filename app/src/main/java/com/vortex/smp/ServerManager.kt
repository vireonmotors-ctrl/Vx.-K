
package com.vortex.smp

import java.io.File

class ServerManager(private val directory: File) {

    fun serverExists(): Boolean {
        return File(directory, "server.jar").exists()
    }

    fun findWorlds(): List<String> {
        return directory.listFiles()
            ?.filter {
                it.isDirectory &&
                File(it, "level.dat").exists()
            }
            ?.map { it.name }
            ?: emptyList()
    }

    fun findPlugins(): List<String> {
        val folder = File(directory, "plugins")

        return folder.listFiles()
            ?.filter { it.isFile && it.extension == "jar" }
            ?.map { it.name }
            ?: emptyList()
    }

    fun javaMemory(): String {
        return "-Xms1G -Xmx4G"
    }
}
