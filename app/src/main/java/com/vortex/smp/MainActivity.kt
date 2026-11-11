
package com.vortex.smp

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.widget.*
import android.graphics.Color
import java.io.File

class MainActivity : Activity() {

    private lateinit var info: TextView
    private lateinit var manager: ServerManager

    private val chooseZip = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            setBackgroundColor(Color.rgb(20, 15, 35))
        }

        val title = TextView(this).apply {
            text = "VORTEX SMP"
            textSize = 30f
            setTextColor(Color.WHITE)
        }

        info = TextView(this).apply {
            text = "Painel de gerenciamento"
            textSize = 16f
            setTextColor(Color.LTGRAY)
        }

        manager = ServerManager(filesDir.resolve("server"))

        root.addView(title)
        root.addView(info)

        addButton(root, "Importar servidor ZIP") {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "application/zip"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            startActivityForResult(intent, chooseZip)
        }

        addButton(root, "Ver mundos") {
            info.text = manager.findWorlds().joinToString("\n")
                .ifEmpty { "Nenhum mundo encontrado." }
        }

        addButton(root, "Ver plugins") {
            info.text = manager.findPlugins().joinToString("\n")
                .ifEmpty { "Nenhum plugin encontrado." }
        }

        addButton(root, "Memória configurada") {
            info.text = manager.javaMemory()
        }

        addButton(root, "Verificar servidor") {
            info.text = if (manager.serverExists()) {
                "Arquivo server.jar encontrado."
            } else {
                "Servidor ainda não instalado."
            }
        }

        setContentView(root)
    }

    private fun addButton(
        root: LinearLayout,
        label: String,
        action: () -> Unit
    ) {
        val button = Button(this)
        button.text = label
        button.setOnClickListener { action() }

        root.addView(
            button,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
    }

    @Deprecated("Deprecated in Android")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == chooseZip && resultCode == RESULT_OK) {
            val uri: Uri = data?.data ?: return

            try {
                val result = ZipImporter.importZip(
                    this,
                    uri,
                    filesDir.resolve("server")
                )
                info.text = result
            } catch (error: Exception) {
                info.text = "Erro na importação: ${error.message}"
            }
        }
    }
}
