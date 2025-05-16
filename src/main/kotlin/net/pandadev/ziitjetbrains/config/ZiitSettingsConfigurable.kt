package net.pandadev.ziitjetbrains.config

import com.intellij.openapi.options.Configurable
import net.pandadev.ziitjetbrains.ui.ZiitSettingsPanel
import javax.swing.JComponent

class ZiitSettingsConfigurable : Configurable {
    private var settingsPanel: ZiitSettingsPanel? = null
    private val config = ZiitConfig.getInstance()

    override fun getDisplayName(): String = "Ziit Configuration"

    override fun createComponent(): JComponent? {
        settingsPanel = ZiitSettingsPanel()
        return settingsPanel?.panel
    }

    override fun isModified(): Boolean {
        return settingsPanel?.apiKeyField?.text != config.getApiKey() ||
               settingsPanel?.instanceUrlField?.text != config.getBaseUrl()
    }

    override fun apply() {
        config.setApiKey(settingsPanel?.apiKeyField?.text)
        settingsPanel?.instanceUrlField?.text?.let { config.setBaseUrl(it) }
    }

    override fun reset() {
        settingsPanel?.apiKeyField?.text = config.getApiKey() ?: ""
        settingsPanel?.instanceUrlField?.text = config.getBaseUrl()
    }

    override fun disposeUIResources() {
        settingsPanel = null
    }
} 