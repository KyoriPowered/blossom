blossom [![Build Status](https://img.shields.io/github/actions/workflow/status/KyoriPowered/blossom/build.yml)](https://github.com/KyoriPowered/blossom/actions) [![License](https://img.shields.io/badge/license-LGPL_v2.1-lightgrey.svg?style=flat)][LGPL v2.1] [![Gradle Plugin](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/net/kyori/blossom/maven-metadata.xml.svg?label=gradle%20plugin&style=flat)](https://plugins.gradle.org/plugin/net.kyori.blossom)
=========
blossom is a Gradle plugin for processing source templates to resources and sources across several languages. It is licensed under the [LGPL v2.1] license.

## Usage
Apply the plugin to your project. Blossom requires a minimum of Java 11. It is tested with the latest revision of the current major release of Gradle, and the latest revision of the previous major release of Gradle, but we focus our development time on newer Gradle versions.

```kotlin
plugins {
  id("net.kyori.blossom") version "2.2.0"
}
```

Blossom adds the `blossom` extension on every source set, allowing the configuration of templating for that source set. No template sets are enabled by default.

Both file names and source files can be templated as desired, using the [Pebble] templating language.

### Resource templating

Call the `resources()` method on the blossom extension to start applying resource templates:

```kotlin
version = "1.4.0-SNAPSHOT"

sourceSets {
  main {
    blossom {
      resources {
        property("version", project.version.toString())
      }
    }
  }
}
```

> [!IMPORTANT]
> When processing templates, Pebble will trim newlines immediately after a template tag.
> This might not be desired when using formats like YAML or Java properties files, so Blossom offers a way to disable this behaviour:
>
> Add a `trimNewlines = false` line to the template set block and stripping will be disabled for any templates in the set.

Then place a file in the `src/main/resource-templates` folder:

`build-vars.properties`:

```properties
version={{ version }}
```

When the project is built, the `build-vars.properties` file will be processed into the final resource:

```properties
version=1.4.0-SNAPSHOT
```

### Source templating

Source templating works similarly, though there is a bit of added complexity due to supporting multiple JVM languages:

`build.gradle.kts`:

```kotlin
sourceSets {
  main {
    blossom {
      javaSources {
        property("version", project.version.toString())
        property("gitCommit", indraGit.commit.map { it.name() }.orNull())
        property("decompilerVersion", libs.versions.vineflower.get())
      }
    }
  }
}
```

`src/main/java-templates/net/kyori/blossomtest/BuildParameters.java.peb`:

```pebble
package net.kyori.blossomtest;

class BuildParameters {
    public static final String VERSION = "{{ version }}";
    public static final String GIT_COMMIT = "{{ gitCommit | default("unknown") }}";
    public static final String DECOMPILER_VERSION = "{{ decompilerVersion }}";
}
```

The `BuildParameters` class will be processed and available to other files being compiled.

## Variants and parameter files, oh my!

While templates on their own allow generating quite a bit, Blossom adds an extra layer of power with *variants*. Each template set can have either the default variant,
or several named variants, which produce output from the same template but different variables as input. When combined with templated file names, this allows generating 
a whole lot of different source files from one input (for example, when working with java primitives).

Properties themselves and property files can be set both for each template set individually, and per-variant.

As an example:

`build.gradle.kts`:

```kotlin
sourceSets {
  main {
    blossom {
      javaSources {
        propertyFile("template-vars.yaml")
        variants("float", "int", "double")
      }
    }
  }
}
```

`template-vars.yaml`:

```yaml
type: potato # shared across variants

# variants key has a special meaning if multiple variants exist - each subkey should match the name of one variant.
# the values under each variant are 
variants:
  float:
    suffix: "f"
    wrapper: "Float"
  int:
    suffix: ""
    wrapper: "Integer"
  double:
    suffix: "d"
    wrapper: "Double"
```

This will process all templates three times, once for each variant -- so a `src/main/java-templates/{{ wrapper }}Box.java.peb` would produce three class files:

- `FloatBox.java`
- `IntegerBox.java`
- `DoubleBox.java`

Template parameters set from different sources (via the DSL) will override each other, inheriting in the following order (where the last element in the list takes priority):

- Default properties provided by Blossom
  - `variant`: provides the variant name as a parameter (only present in named variant mode)
- Template set, defined in a set property file
- Template set, defined in-buildscript
- Variant, defined in global files
- Variant, defined in the variant-specific property files
- Variant, defined in-buildscript

## IDE Integration

On first import into an IDE, you may have to run the `generateTemplates` task to ensure templates have been generated. For some common IDEs, we hook into the IDE's refresh system in order to 

### Eclipse

In Eclipse, this task is registered as a "synchronization" task, which will update templates every time the project is synced with Gradle.

### IntelliJ

For IntelliJ integration, also add the [`org.jetbrains.gradle.plugin.idea-ext`](https://github.com/JetBrains/gradle-idea-ext-plugin) plugin, and Blossom will automatically configure the appropriate post-import hooks. Eclipse integration requires no other plugins.

There is some IDE support for the Pebble syntax. An [IntelliJ plugin](https://plugins.jetbrains.com/plugin/9407-pebble) exists, though it does
little more than syntax highlighting.

IntelliJ also has a setting that attempts to highlight template files as Java source files
(available under Preferences > Languages & Frameworks > Template Data Languages). This option is of varying effectiveness depending on the source file.

### Others

On other IDEs, there is no current support, but we are open to adding such support if there's a way -- open an issue if you use an IDE with such facilities that is not yet supported.


[Pebble]: https://pebbletemplates.io/
[LGPL v2.1]: https://choosealicense.com/licenses/lgpl-2.1/
