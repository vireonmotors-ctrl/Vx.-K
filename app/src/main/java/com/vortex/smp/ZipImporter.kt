
package com.vortex.smp

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.io.File

class MainActivity : Activity() {

    private lateinit var statusText: TextView
    private lateinit var resultText: TextView
    private var importedFolder: File? = null

    companion object {
        private const val REQUEST_ZIP = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
        }

        val title = TextView(this).apply {
            text = "VORTEX SMP"
            textSize = 28f
        }

        statusText = TextView(this).apply {
            text = "Importe seu servidor ZIP."
            textSize = 16f
        }

        resultText = TextView(this).apply {
            textSize = 15f
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
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/zip"
            }
            startActivityForResult(intent, REQUEST_ZIP)
        }

        addButton("VER PLUGINS") { showPlugins() }
        addButton("VER MUNDOS") { showWorlds() }
        addButton("VERIFICAR SERVIDOR") { showServer() }

        addButton("MEMÓRIA CONFIGURADA") {
            resultText.text =
                "Configuração desejada: -Xms1G -Xmx4G\n" +
                "Isso não inicia o servidor nem garante 4 GB disponíveis."
        }

        layout.addView(resultText)

        val scroll = ScrollView(this)
        scroll.addView(layout)
        setContentView(scroll)
    }

    @Deprecated("Usa o seletor nativo do Android")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_ZIP && resultCode == RESULT_OK) {
            val uri: Uri = data?.data ?: return

            try {
                val folder = ZipImporter.importZip(this, uri)
                importedFolder = ServerManager.findServerRoot(folder)

                statusText.text = "ZIP importado com sucesso!"
                showServer()

                Toast.makeText(
                    this,
                    "Servidor importado!",
                    Toast.LENGTH_LONG
                ).show()

            } catch (e: Exception) {
                statusText.text = "Erro: ${e.message}"
            }
        }
    }

    private fun showPlugins() {
        val root = importedFolder

        if (root == null) {
            resultText.text = "Importe um ZIP primeiro."
            return
        }

        val plugins = ServerManager.findPlugins(root)

        resultText.text = if (plugins.isEmpty()) {
            "Nenhum plugin .jar encontrado."
        } else {
            "PLUGINS ENCONTRADOS: ${plugins.size}\n\n" +
                plugins.joinToString("\n") {
                    it.name
                }
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
            "Nenhum mundo com level.dat encontrado."
        } else {
            "MUNDOS ENCONTRADOS: ${worlds.size}\n\n" +
                worlds.joinToString("\n") {
                    it.absolutePath
                }
        }
    }

    private fun showServer() {
        val root = importedFolder

        if (root == null) {
            resultText.text = "Nenhum servidor importado."
            return
        }

        val plugins = ServerManager.findPlugins(root)
        val worlds = ServerManager.findWorlds(root)
        val jars = ServerManager.findServerJars(root)

        resultText.text = """
            PASTA: ${root.absolutePath}

            PLUGINS: ${plugins.size}
            MUNDOS: ${worlds.size}
            ARQUIVOS JAR: ${jars.size}

            ARQUIVOS DO SERVIDOR:
            ${jars.joinToString("\n") { it.name }.ifEmpty { "Nenhum encontrado" }}

            Memória desejada: 4 GB

            Esta versão identifica arquivos,
            mas ainda não inicia o servidor.
        """.trimIndent()
    }
}
