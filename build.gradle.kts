import com.diffplug.gradle.spotless.FormatExtension
import org.jetbrains.gradle.ext.settings
import org.jetbrains.gradle.ext.taskTriggers

plugins {
  `java-gradle-plugin`
  alias(libs.plugins.indra)
  alias(libs.plugins.indra.gradlePluginPublish)
  alias(libs.plugins.indra.licenserSpotless)
  alias(libs.plugins.indra.checkstyle)
  alias(libs.plugins.pluginPublish)
  alias(libs.plugins.spotless)
  alias(libs.plugins.ideaExt)
  eclipse
}

repositories {
  mavenCentral()
  gradlePluginPortal()
}

// Worker hijinks
// workerShared available at compile time in workerClasspath and main
// workerClasspath is isolated for worker-only deps
val workerShared by sourceSets.registering
val workerClasspath by sourceSets.registering {
  compileClasspath += sourceSets.main.get().compileClasspath
}

val privateRuntime by configurations.registering
configurations.runtimeClasspath {
  extendsFrom(privateRuntime.get())
}

// Register worker bits to the unpacked variant
listOf(configurations.apiElements, configurations.runtimeElements).forEach {
  it.configure {
    outgoing.variants.named("classes") {
      artifact(workerClasspath.flatMap { it.java.destinationDirectory}) {
        type = ArtifactTypeDefinition.JVM_CLASS_DIRECTORY
      }
      artifact(workerShared.flatMap { it.java.destinationDirectory}) {
        type = ArtifactTypeDefinition.JVM_CLASS_DIRECTORY
      }
    }
  }
}

// Then add them to the jar
tasks.jar {
  from(workerShared.map { it.output })
  from(workerClasspath.map { it.output })
}

dependencies {
  implementation(libs.mammoth)
  "workerClasspathCompileOnly"(libs.pebble)
  "workerClasspathCompileOnly"(libs.snakeyamlEngine)
  "workerClasspathCompileOnly"(workerShared.map { it.output })
  compileOnly(workerShared.map { it.output })
  privateRuntime.name(workerClasspath.map { it.output })
  privateRuntime.name(workerShared.map { it.output })
  compileOnly(libs.ideaExtPlugin)

  testImplementation(libs.mammoth.test)
  testImplementation(platform(libs.junit.bom))
  testImplementation(libs.junit.api)
  testRuntimeOnly(libs.junit.engine)
  testRuntimeOnly(libs.junit.launcher)

  checkstyle(libs.stylecheck)
}

// generated sources (blossom jr)
val templatesRoot = layout.projectDirectory.dir("src/main/java-templates")
val templateDest = layout.buildDirectory.dir("generated/sources/java-templates/")

val processTemplates = tasks.register("generateJavaTemplates", Sync::class) {
  val templateProperties = mapOf(
    "pebbleVersion" to libs.versions.pebble.get(),
    "snakeyamlVersion" to libs.versions.snakeyaml.get()
  )
  inputs.properties(templateProperties)

  from(templatesRoot)
  into(templateDest)
  expand(templateProperties.toMutableMap())
}

sourceSets.main {
  java.srcDir(processTemplates.map { it.outputs })
}
eclipse.synchronizationTasks(processTemplates)
if (idea.project != null) {
  setOf(idea.project.settings.taskTriggers.afterSync(processTemplates))
}
if (idea.module != null) {
  idea.module.generatedSourceDirs.add(templateDest.get().asFile)
}

indra {
  github("KyoriPowered", "blossom") {
    ci(true)
  }

  javaVersions {
    target(11)
    minimumToolchain(17)
    testWith(11, 17)
  }
  checkstyle(libs.versions.checkstyle.get())

  signWithKeyFromPrefixedProperties("kyori")
  configurePublications {
    pom {
      url = "https://blossom.kyori.net"
      organization {
        name = "KyoriPowered"
        url = "https://kyori.net"
      }

      developers {
        developer {
          id = "zml"
          email = "zml at kyori [.] net"
          timezone = "America/Vancouver"
        }
      }
    }
  }
}

indraPluginPublishing {
  plugin(
    "blossom",
    "net.kyori.blossom.Blossom",
    "blossom",
    project.description,
    listOf("templating", "replacement")
  )
  website("https://blossom.kyori.net/")
}

spotless {
  fun FormatExtension.applyCommon() {
    endWithNewline()
    indentWithSpaces(2)
    trimTrailingWhitespace()
  }
  java {
    targetExclude("build/generated/**")
    applyCommon()
    importOrderFile(rootProject.file(".spotless/kyori.importorder"))
  }
  kotlinGradle {
    applyCommon()
  }
}
