package net.pandadev.ziitjetbrains.config

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.Messages
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

@Service
class ZiitConfig {
    companion object {
        private const val CONFIG_FILE_NAME = ".ziit.cfg"
        private const val API_KEY = "ziit.apiKey"
        private const val BASE_URL = "ziit.baseUrl"
        private const val ENABLED = "ziit.enabled"
        private const val STATUS_BAR_ENABLED = "ziit.statusBarEnabled"
        private const val DEBUG = "ziit.debug"
        private const val KEYSTROKE_TIMEOUT = "ziit.keystrokeTimeout"

        private val CONFIG_FILE_PATH = Paths.get(System.getProperty("user.home"), CONFIG_FILE_NAME)

        fun getInstance(): ZiitConfig = service()
    }

    private val properties = PropertiesComponent.getInstance()


    init {
        initializeAndSyncConfig()
    }

    fun getApiKey(): String? {
        return properties.getValue(API_KEY)
    }

    fun setApiKey(apiKey: String?) {
        properties.setValue(API_KEY, apiKey)
        updateConfigFile(API_KEY, apiKey)
    }

    fun getBaseUrl(): String {
        return properties.getValue(BASE_URL, "https://ziit.app")
    }

    fun setBaseUrl(baseUrl: String) {
        properties.setValue(BASE_URL, baseUrl)
        updateConfigFile(BASE_URL, baseUrl)
    }

    fun isEnabled(): Boolean {
        return properties.getBoolean(ENABLED, true)
    }

    fun setEnabled(enabled: Boolean) {
        properties.setValue(ENABLED, enabled, true)
        updateConfigFile(ENABLED, enabled.toString())
    }

    fun isStatusBarEnabled(): Boolean {
        return properties.getBoolean(STATUS_BAR_ENABLED, true)
    }

    fun setStatusBarEnabled(enabled: Boolean) {
        properties.setValue(STATUS_BAR_ENABLED, enabled, true)
        updateConfigFile(STATUS_BAR_ENABLED, enabled.toString())
    }

    fun isDebugEnabled(): Boolean {
        return properties.getBoolean(DEBUG, false)
    }

    fun setDebugEnabled(enabled: Boolean) {
        properties.setValue(DEBUG, enabled, false)
        updateConfigFile(DEBUG, enabled.toString())
    }

    fun getKeystrokeTimeout(): Int? {
        val value = properties.getValue(KEYSTROKE_TIMEOUT) ?: return null
        return try {
            value.toInt()
        } catch (e: NumberFormatException) {
            null
        }
    }

    fun setKeystrokeTimeout(timeoutMinutes: Int?) {
        properties.setValue(KEYSTROKE_TIMEOUT, timeoutMinutes?.toString())
        updateConfigFile(KEYSTROKE_TIMEOUT, timeoutMinutes?.toString())
    }

    private fun initializeAndSyncConfig() {
        val configFile = File(CONFIG_FILE_PATH.toString())

        if (!configFile.exists()) {
            createConfigFile()
        } else {
            syncFromConfigFile()
        }
    }

    private fun createConfigFile() {
        try {
            val content = buildConfigFileContent()
            Files.write(CONFIG_FILE_PATH, content.toByteArray())
        } catch (e: Exception) {

        }
    }

    private fun syncFromConfigFile() {
        try {
            val properties = Properties()
            Files.newInputStream(CONFIG_FILE_PATH).use { input ->
                properties.load(input)
            }


            transferProperty(properties, "api_key", API_KEY)
            transferProperty(properties, "base_url", BASE_URL, "https://ziit.app")
            transferBooleanProperty(properties, "enabled", ENABLED, true)
            transferBooleanProperty(properties, "status_bar_enabled", STATUS_BAR_ENABLED, true)
            transferBooleanProperty(properties, "debug", DEBUG, false)
            transferProperty(properties, "keystroke_timeout", KEYSTROKE_TIMEOUT)
        } catch (e: Exception) {

        }
    }

    private fun transferProperty(
        fileProps: Properties,
        fileKey: String,
        ideaKey: String,
        defaultValue: String? = null
    ) {
        val value = fileProps.getProperty(fileKey) ?: defaultValue
        if (value != null) {
            properties.setValue(ideaKey, value)
        }
    }

    private fun transferBooleanProperty(
        fileProps: Properties,
        fileKey: String,
        ideaKey: String,
        defaultValue: Boolean
    ) {
        val value = fileProps.getProperty(fileKey)?.lowercase() == "true"
        properties.setValue(ideaKey, value, defaultValue)
    }

    private fun updateConfigFile(key: String, value: String?) {
        try {
            val configFile = File(CONFIG_FILE_PATH.toString())
            if (!configFile.exists()) {
                createConfigFile()
                return
            }

            val props = Properties()
            Files.newInputStream(CONFIG_FILE_PATH).use { input ->
                props.load(input)
            }


            val fileKey = when (key) {
                API_KEY -> "api_key"
                BASE_URL -> "base_url"
                ENABLED -> "enabled"
                STATUS_BAR_ENABLED -> "status_bar_enabled"
                DEBUG -> "debug"
                KEYSTROKE_TIMEOUT -> "keystroke_timeout"
                else -> return
            }

            if (value == null) {
                props.remove(fileKey)
            } else {
                props.setProperty(fileKey, value)
            }

            Files.newOutputStream(CONFIG_FILE_PATH).use { output ->
                props.store(output, "Ziit Configuration")
            }
        } catch (e: Exception) {

        }
    }

    private fun buildConfigFileContent(): String {
        val sb = StringBuilder("[settings]\n")

        properties.getValue(API_KEY)?.let { sb.append("api_key = $it\n") }
        sb.append("base_url = ${properties.getValue(BASE_URL, "https://ziit.app")}\n")
        sb.append("enabled = ${properties.getBoolean(ENABLED, true)}\n")
        sb.append("status_bar_enabled = ${properties.getBoolean(STATUS_BAR_ENABLED, true)}\n")
        sb.append("debug = ${properties.getBoolean(DEBUG, false)}\n")
        properties.getValue(KEYSTROKE_TIMEOUT)?.let { sb.append("keystroke_timeout = $it\n") }

        return sb.toString()
    }

    fun promptForApiKey(): String? {
        val apiKey = Messages.showInputDialog(
            "Enter your Ziit API key",
            "Ziit API Key",
            null
        )
        if (apiKey != null && apiKey.isNotEmpty()) {
            setApiKey(apiKey)
        }
        return apiKey
    }

    fun promptForBaseUrl(): String? {
        val baseUrl = Messages.showInputDialog(
            "Enter your Ziit instance URL",
            "Ziit Instance URL",
            null,
            getBaseUrl(),
            null
        )
        if (baseUrl != null && baseUrl.isNotEmpty()) {
            setBaseUrl(baseUrl)
        }
        return baseUrl
    }
} 