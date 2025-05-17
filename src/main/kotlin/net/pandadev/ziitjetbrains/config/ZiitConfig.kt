package net.pandadev.ziitjetbrains.config

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.Messages
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import org.json.JSONObject

@Service
class ZiitConfig {
    companion object {
        private const val CONFIG_FILE_NAME = ".ziit.json"
        private const val OLD_CONFIG_FILE_NAME = ".ziit.cfg"
        private val CONFIG_FILE_PATH = Paths.get(System.getProperty("user.home"), CONFIG_FILE_NAME)
        private val OLD_CONFIG_FILE_PATH = Paths.get(System.getProperty("user.home"), OLD_CONFIG_FILE_NAME)
        private const val API_KEY = "ziit.apiKey"
        private const val BASE_URL = "ziit.baseUrl"
        fun getInstance(): ZiitConfig = service()
    }

    private val properties = PropertiesComponent.getInstance()

    init {
        migrateOldConfigIfNeeded()
        initializeAndSyncConfig()
    }

    fun getApiKey(): String? {
        return properties.getValue(API_KEY)
    }

    fun setApiKey(apiKey: String?) {
        properties.setValue(API_KEY, apiKey)
        updateConfigFile()
    }

    fun getBaseUrl(): String {
        return properties.getValue(BASE_URL, "https://ziit.app")
    }

    fun setBaseUrl(baseUrl: String) {
        properties.setValue(BASE_URL, baseUrl)
        updateConfigFile()
    }

    private fun migrateOldConfigIfNeeded() {
        val oldFile = File(OLD_CONFIG_FILE_PATH.toString())
        if (!oldFile.exists()) return
        var apiKey: String? = null
        var baseUrl: String? = null
        oldFile.readLines().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.startsWith("api_key")) {
                apiKey = trimmed.split("=").getOrNull(1)?.trim()
            }
            if (trimmed.startsWith("base_url")) {
                baseUrl = trimmed.split("=").getOrNull(1)?.trim()?.replace("\\:", ":")
            }
        }
        val json = JSONObject()
        if (!apiKey.isNullOrEmpty()) json.put("apiKey", apiKey)
        if (!baseUrl.isNullOrEmpty()) json.put("baseUrl", baseUrl)
        Files.write(CONFIG_FILE_PATH, json.toString(2).toByteArray())
        oldFile.delete()
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
            val json = JSONObject()
            getApiKey()?.let { json.put("apiKey", it) }
            json.put("baseUrl", getBaseUrl())
            Files.write(CONFIG_FILE_PATH, json.toString(2).toByteArray())
        } catch (_: Exception) {}
    }

    private fun syncFromConfigFile() {
        try {
            val content = Files.readAllBytes(CONFIG_FILE_PATH).toString(Charsets.UTF_8)
            val json = JSONObject(content)
            if (json.has("apiKey")) properties.setValue(API_KEY, json.getString("apiKey"))
            if (json.has("baseUrl")) properties.setValue(BASE_URL, json.getString("baseUrl"))
        } catch (_: Exception) {}
    }

    private fun updateConfigFile() {
        try {
            val json = JSONObject()
            getApiKey()?.let { json.put("apiKey", it) }
            json.put("baseUrl", getBaseUrl())
            Files.write(CONFIG_FILE_PATH, json.toString(2).toByteArray())
        } catch (_: Exception) {}
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