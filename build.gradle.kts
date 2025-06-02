plugins {
  id("java")
  id("org.jetbrains.kotlin.jvm") version "2.2.0-RC"
  id("org.jetbrains.intellij.platform") version "2.6.0"
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
    create("IC", "2025.1")
    testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
  }

  implementation("org.json:json:20240303")
}

intellijPlatform {
  pluginConfiguration {
    ideaVersion {
      sinceBuild = "242"
      untilBuild = "251.*"
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

val extractJsonLibrary by tasks.registering(Copy::class) {
  from(configurations.runtimeClasspath.get()
    .filter { it.name.contains("json") }
    .map { zipTree(it) })
  into(layout.buildDirectory.dir("json-lib"))
}

tasks {
  withType<JavaCompile> {
    sourceCompatibility = "17"
    targetCompatibility = "17"
  }
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
      jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
      apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
      languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
    }
  }
  
  jar {
    dependsOn(extractJsonLibrary)
    from(layout.buildDirectory.dir("json-lib"))
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
  }
  
  prepareSandbox {
    dependsOn(jar)
  }
}