package net.pandadev.ziitjetbrains

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.ui.Messages
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import org.json.JSONObject
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Service
class Config {
    companion object {
        private const val CONFIG_FILE_NAME = "config.json"
        private const val LEGACY_CONFIG_FILE_NAME = ".ziit.json"
        private const val OLD_CONFIG_FILE_NAME = ".ziit.cfg"

        private val CONFIG_DIR = getConfigDir()
        private val CONFIG_FILE_PATH = CONFIG_DIR.resolve(CONFIG_FILE_NAME)
        private val LEGACY_CONFIG_FILE_PATH = Paths.get(System.getProperty("user.home"), LEGACY_CONFIG_FILE_NAME)
        private val OLD_CONFIG_FILE_PATH = Paths.get(System.getProperty("user.home"), OLD_CONFIG_FILE_NAME)

        private const val API_KEY = "ziit.apiKey"
        private const val BASE_URL = "ziit.baseUrl"

        private fun getConfigDir(): Path {
            val xdgConfigHome = System.getenv("XDG_CONFIG_HOME")
            return if (xdgConfigHome != null && xdgConfigHome.isNotEmpty()) {
                Paths.get(xdgConfigHome, "ziit")
            } else {
                Paths.get(System.getProperty("user.home"), ".config", "ziit")
            }
        }

        fun getInstance(): Config = service()
    }

    private val properties = PropertiesComponent.getInstance()
    private val logger = logger<Config>()

    init {
        ensureConfigDir()
        migrateLegacyConfigs()
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

    private fun ensureConfigDir() {
        try {
            Files.createDirectories(CONFIG_DIR)
        } catch (e: Exception) {
            logger.warn("Failed to create config directory: ${e.message}", e)
        }
    }

    private fun migrateLegacyConfigs() {
        if (Files.exists(CONFIG_FILE_PATH)) {
            return
        }

        var migratedConfig: JSONObject? = null
        var migrationSource: String? = null

        val legacyFile = File(LEGACY_CONFIG_FILE_PATH.toString())
        if (legacyFile.exists()) {
            try {
                val content = legacyFile.readText()
                migratedConfig = JSONObject(content)
                migrationSource = LEGACY_CONFIG_FILE_PATH.toString()
            } catch (e: Exception) {
                logger.warn("Failed to parse legacy JSON config file: ${e.message}", e)
            }
        }

        if (migratedConfig == null) {
            val oldFile = File(OLD_CONFIG_FILE_PATH.toString())
            if (oldFile.exists()) {
                try {
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
                    migratedConfig = JSONObject()
                    if (!apiKey.isNullOrEmpty()) migratedConfig!!.put("apiKey", apiKey)
                    if (!baseUrl.isNullOrEmpty()) migratedConfig!!.put("baseUrl", baseUrl)
                    migrationSource = OLD_CONFIG_FILE_PATH.toString()
                } catch (e: Exception) {
                    logger.warn("Failed to parse legacy .ziit.cfg config file: ${e.message}", e)
                }
            }
        }

        if (migratedConfig != null && migrationSource != null) {
            try {
                Files.write(CONFIG_FILE_PATH, migratedConfig.toString(2).toByteArray())

                if (migratedConfig.has("apiKey")) {
                    properties.setValue(API_KEY, migratedConfig.getString("apiKey"))
                }
                if (migratedConfig.has("baseUrl")) {
                    properties.setValue(BASE_URL, migratedConfig.getString("baseUrl"))
                }

                try {
                    when (migrationSource) {
                        LEGACY_CONFIG_FILE_PATH.toString() -> {
                            legacyFile.delete()
                        }

                        OLD_CONFIG_FILE_PATH.toString() -> {
                            File(OLD_CONFIG_FILE_PATH.toString()).delete()
                        }
                    }
                } catch (e: Exception) {
                    logger.warn("Failed to clean up legacy config file during migration: ${e.message}", e)
                }

                logger.info("Successfully migrated Ziit configuration from $migrationSource to $CONFIG_FILE_PATH")
                NotificationGroupManager.getInstance()
                    .getNotificationGroup("Ziit Notifications")
                    .createNotification(
                        "Ziit Configuration Migrated",
                        "Your Ziit configuration has been migrated to the new XDG-compliant location: $CONFIG_FILE_PATH",
                        NotificationType.INFORMATION
                    )
                    .notify(null)

            } catch (e: Exception) {
                logger.error("Failed to migrate legacy config: ${e.message}", e)
                NotificationGroupManager.getInstance()
                    .getNotificationGroup("Ziit Notifications")
                    .createNotification(
                        "Ziit Configuration Migration Failed",
                        "Failed to migrate Ziit configuration: ${e.message}",
                        NotificationType.ERROR
                    )
                    .notify(null)
            }
        }
    }

    private fun initializeAndSyncConfig() {
        val configFile = File(CONFIG_FILE_PATH.toString())
        if (!configFile.exists()) {
            updateConfigFile()
        } else {
            syncFromConfigFile()
        }
    }

    private fun syncFromConfigFile() {
        try {
            val content = Files.readAllBytes(CONFIG_FILE_PATH).toString(Charsets.UTF_8)
            val json = JSONObject(content)
            if (json.has("apiKey")) properties.setValue(API_KEY, json.getString("apiKey"))
            if (json.has("baseUrl")) properties.setValue(BASE_URL, json.getString("baseUrl"))
        } catch (e: Exception) {
            logger.warn("Failed to sync from config file: ${e.message}", e)
        }
    }

    private fun updateConfigFile() {
        try {
            val json = JSONObject()
            getApiKey()?.let { json.put("apiKey", it) }
            json.put("baseUrl", getBaseUrl())
            Files.write(CONFIG_FILE_PATH, json.toString(2).toByteArray())
        } catch (e: Exception) {
            logger.warn("Failed to update config file: ${e.message}", e)
        }
    }

    fun promptForApiKey(): String? {
        val apiKey = Messages.showInputDialog(
            "Enter your Ziit API key", "Ziit API Key", null
        )
        if (apiKey != null && apiKey.isNotEmpty()) {
            setApiKey(apiKey)
        }
        return apiKey
    }

    fun promptForBaseUrl(): String? {
        val baseUrl = Messages.showInputDialog(
            "Enter your Ziit instance URL", "Ziit Instance URL", null, getBaseUrl(), null
        )
        if (baseUrl != null && baseUrl.isNotEmpty()) {
            setBaseUrl(baseUrl)
        }
        return baseUrl
    }
}
