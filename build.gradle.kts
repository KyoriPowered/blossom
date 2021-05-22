plugins {
  id("java-gradle-plugin")
  id("com.gradle.plugin-publish") version "0.13.0"
  val indraVersion = "2.0.4"
  id("net.kyori.indra") version indraVersion
  id("net.kyori.indra.license-header") version indraVersion
  id("net.kyori.indra.publishing.gradle-plugin") version indraVersion
  id("net.kyori.indra.checkstyle") version indraVersion
}

group = "net.kyori"
version = "1.3.0-SNAPSHOT"
description = "Gradle plugin for performing source code token replacements in Java, Kotlin, Scala, and Groovy based projects"

repositories {
  mavenCentral()
  gradlePluginPortal()
}

dependencies {
  implementation("net.kyori", "mammoth", "1.0.0")
  implementation("com.google.guava", "guava", "30.1.1-jre")
  implementation("org.jetbrains.kotlin", "kotlin-gradle-plugin-api", "1.4.31")
  compileOnly("org.checkerframework", "checker-qual", "3.13.0")

  checkstyle("ca.stellardrift", "stylecheck", "0.1")
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
