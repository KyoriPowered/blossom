plugins {
  id("java-gradle-plugin")
  id("com.gradle.plugin-publish")
  id("net.kyori.indra")
  id("net.kyori.indra.license-header")
  id("net.kyori.indra.publishing.gradle-plugin")
  id("net.kyori.indra.checkstyle")
}

group = "net.kyori"
version = "1.3.0"
description = "Gradle plugin for performing source code token replacements in Java, Kotlin, Scala, and Groovy based projects"

repositories {
  mavenCentral()
  gradlePluginPortal()
}

dependencies {
  implementation(libs.mammoth)
  implementation(libs.guava)
  implementation(libs.kotlinGradlePluginApi)
  compileOnly(libs.checkerQual)
  checkstyle(libs.stylecheck)
}

indra {
  javaVersions {
    target(8)
    testWith(8, 11, 16)
  }
  github("KyoriPowered", "blossom")
}

license {
  exclude("**/net/kyori/blossom/task/SourceReplacementTask.java")
}

indraPluginPublishing {
  plugin(
    "blossom",
    "net.kyori.blossom.Blossom",
    "blossom",
    project.description,
    listOf("blossom", "replacement")
  )
  website("https://github.com/KyoriPowered/blossom")
}
