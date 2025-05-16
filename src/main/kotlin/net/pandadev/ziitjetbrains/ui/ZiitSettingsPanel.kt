package net.pandadev.ziitjetbrains.ui

import com.intellij.ui.dsl.builder.panel
import javax.swing.JPanel
import javax.swing.JTextField

class ZiitSettingsPanel {
    val apiKeyField = JTextField()
    val instanceUrlField = JTextField()

    val panel: JPanel = panel {
        row("API Key:") {
            cell(apiKeyField)
                .resizableColumn()
                .comment("Your Ziit API key.")
        }
        row("Instance URL:") {
            cell(instanceUrlField)
                .resizableColumn()
                .comment("Your Ziit instance URL (e.g., https://ziit.app).")
        }
    }
} 