import com.diffplug.gradle.spotless.FormatExtension

plugins {
  id("java-gradle-plugin")
  alias(libs.plugins.indra)
  alias(libs.plugins.indra.gradlePluginPublish)
  alias(libs.plugins.indra.licenserSpotless)
  alias(libs.plugins.indra.checkstyle)
  alias(libs.plugins.pluginPublish)
  alias(libs.plugins.spotless)
}

group = "net.kyori"
version = "2.0.0-SNAPSHOT"
description = "Gradle plugin for performing resource and source code template expansion"

repositories {
  mavenCentral()
  gradlePluginPortal()
}

dependencies {
  implementation(libs.mammoth)
  implementation(libs.pebble)
  implementation(libs.snakeyamlEngine)

  testImplementation(libs.mammoth.test)
  testImplementation(platform(libs.junit.bom))
  testImplementation(libs.junit.api)
  testRuntimeOnly(libs.junit.engine)
  testRuntimeOnly(libs.junit.launcher)

  checkstyle(libs.stylecheck)
}

indra {
  github("KyoriPowered", "blossom")
  javaVersions {
    target(11)
    minimumToolchain(17)
    testWith(11, 17, 20)
  }
  checkstyle(libs.versions.checkstyle.get())
}

indraPluginPublishing {
  plugin(
    "blossom",
    "net.kyori.blossom.Blossom",
    "blossom",
    project.description,
    listOf("templating", "replacement")
  )
  website("https://github.com/KyoriPowered/blossom")
}

spotless {
  fun FormatExtension.applyCommon() {
    endWithNewline()
    indentWithSpaces(2)
    trimTrailingWhitespace()
  }
  java {
    applyCommon()
    importOrderFile(rootProject.file(".spotless/kyori.importorder"))
  }
  kotlinGradle {
    applyCommon()
  }
}
