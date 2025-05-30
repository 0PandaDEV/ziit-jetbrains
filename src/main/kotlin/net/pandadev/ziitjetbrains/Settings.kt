package net.pandadev.ziitjetbrains

import com.intellij.openapi.options.Configurable
import com.intellij.ui.dsl.builder.COLUMNS_LARGE
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.columns
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextField

class Settings : Configurable {
    val apiKeyField = JTextField()
    val instanceUrlField = JTextField()
    private val config = Config.getInstance()

    val panel: JPanel = panel {
        row("API Key:") {
            cell(apiKeyField)
                .comment("Your Ziit API key.")
                .columns(COLUMNS_LARGE)
        }
        row("Instance URL:") {
            cell(instanceUrlField)
                .comment("Your Ziit instance URL (e.g., https://ziit.app).")
                .columns(COLUMNS_LARGE)
        }
    }

    override fun getDisplayName(): String = "Ziit"

    override fun createComponent(): JComponent? {
        return panel
    }

    override fun isModified(): Boolean {
        return apiKeyField.text != config.getApiKey() ||
                instanceUrlField.text != config.getBaseUrl()
    }

    override fun apply() {
        config.setApiKey(apiKeyField.text)
        instanceUrlField.text.let { config.setBaseUrl(it) }
    }

    override fun reset() {
        apiKeyField.text = config.getApiKey() ?: ""
        instanceUrlField.text = config.getBaseUrl()
    }
} 