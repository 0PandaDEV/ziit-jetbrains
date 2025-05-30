plugins {
  id("java")
  id("org.jetbrains.kotlin.jvm") version "1.9.25"
  id("org.jetbrains.intellij.platform") version "2.3.0"
}

group = "net.pandadev"
version = "1.0.0"

repositories {
  mavenCentral()
  intellijPlatform {
    defaultRepositories()
  }
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
dependencies {
  intellijPlatform {
    create("IC", "2024.2.5")
    testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)

    // Add necessary plugin dependencies for compilation here, example:
    // bundledPlugin("com.intellij.java")
  }
  
  // JSON library
  implementation("org.json:json:20240303")
}

intellijPlatform {
  pluginConfiguration {
    ideaVersion {
      sinceBuild = "242"
    }

    changeNotes = """
      Initial version with time tracking functionality:
      - Automatic time tracking while you code
      - Status bar integration showing daily coding time
      - Offline support for continued tracking when not connected
      - Integration with Ziit dashboard
    """.trimIndent()
  }
}

// Extract JSON library and include it in the plugin jar
val extractJsonLibrary by tasks.registering(Copy::class) {
  from(configurations.runtimeClasspath.get()
    .filter { it.name.contains("json") }
    .map { zipTree(it) })
  into(layout.buildDirectory.dir("json-lib"))
}

tasks {
  // Set the JVM compatibility versions
  withType<JavaCompile> {
    sourceCompatibility = "17"
    targetCompatibility = "17"
  }
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
    kotlinOptions.apiVersion = "1.9"
    kotlinOptions.languageVersion = "1.9"
  }
  
  // Include JSON library classes directly in the plugin jar
  jar {
    dependsOn(extractJsonLibrary)
    from(layout.buildDirectory.dir("json-lib"))
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
  }
  
  prepareSandbox {
    dependsOn(jar)
  }
}