
package com.vortex.smp

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File

class MainActivity : Activity() {

    private lateinit var statusText: TextView
    private lateinit var resultText: TextView
    private var importedFolder: File? = null

    private val zipPicker = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val folder = ZipImporter.importZip(this, uri)
                importedFolder = ServerManager.findServerRoot(folder)

                statusText.text = "ZIP importado com sucesso!"
                showServerContents()
                Toast.makeText(
                    this,
                    "Servidor importado!",
                    Toast.LENGTH_LONG
                ).show()

            } catch (e: Exception) {
                statusText.text = "Erro ao importar ZIP: ${e.message}"
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 30, 30, 30)
        }

        val title = TextView(this).apply {
            text = "VORTEX SMP"
            textSize = 28f
        }

        statusText = TextView(this).apply {
            text = "Importe seu servidor Minecraft ZIP."
            textSize = 16f
            setPadding(0, 20, 0, 20)
        }

        resultText = TextView(this).apply {
            textSize = 15f
            setPadding(0, 20, 0, 20)
        }

        fun addButton(label: String, action: () -> Unit) {
            val button = Button(this).apply {
                text = label
                setOnClickListener { action() }
            }
            layout.addView(button)
        }

        layout.addView(title)
        layout.addView(statusText)

        addButton("IMPORTAR SERVIDOR ZIP") {
            zipPicker.launch(
                arrayOf("application/zip", "application/octet-stream", "*/*")
            )
        }

        addButton("VER PLUGINS") {
            showPlugins()
        }

        addButton("VER MUNDOS") {
            showWorlds()
        }

        addButton("VERIFICAR SERVIDOR") {
            showServerContents()
        }

        addButton("MEMÓRIA CONFIGURADA") {
            resultText.text =
                "Memória configurada:\n${ServerManager.memoryArgs().joinToString(" ")}\n\nA memória real depende da RAM disponível."
        }

        layout.addView(resultText)

        val scroll = ScrollView(this)
        scroll.addView(layout)
        setContentView(scroll)
    }

    private fun showPlugins() {
        val root = importedFolder

        if (root == null) {
            resultText.text = "Importe um ZIP primeiro."
            return
        }

        val plugins = ServerManager.findPlugins(root)

        resultText.text = if (plugins.isEmpty()) {
            "Nenhum plugin .jar encontrado em pastas chamadas plugins."
        } else {
            "PLUGINS ENCONTRADOS: ${plugins.size}\n\n" +
                plugins.joinToString("\n") { it.name }
        }
    }

    private fun showWorlds() {
        val root = importedFolder

        if (root == null) {
            resultText.text = "Importe um ZIP primeiro."
            return
        }

        val worlds = ServerManager.findWorlds(root)

        resultText.text = if (worlds.isEmpty()) {
            "Nenhum mundo Java com level.dat encontrado."
        } else {
            "MUNDOS ENCONTRADOS: ${worlds.size}\n\n" +
                worlds.joinToString("\n") { it.absolutePath }
        }
    }

    private fun showServerContents() {
        val root = importedFolder

        if (root == null) {
            resultText.text = "Nenhum servidor importado."
            return
        }

        val plugins = ServerManager.findPlugins(root)
        val worlds = ServerManager.findWorlds(root)
        val jars = ServerManager.findServerJars(root)

        resultText.text = """
            PASTA DO SERVIDOR:
            ${root.absolutePath}

            PLUGINS: ${plugins.size}
            MUNDOS: ${worlds.size}
            ARQUIVOS JAR DO SERVIDOR: ${jars.size}

            ARQUIVOS DO SERVIDOR:
            ${jars.joinToString("\n") { it.name }.ifEmpty { "Nenhum encontrado" }}

            MEMÓRIA CONFIGURADA:
            ${ServerManager.memoryArgs().joinToString(" ")}

            Observação: esta tela identifica os arquivos.
            Ela ainda não inicia o servidor Minecraft.
        """.trimIndent()
    }
}
