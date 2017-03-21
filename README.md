Blossom [![Build Status](https://travis-ci.org/MiserableNinja/Blossom.svg?branch=master)](https://travis-ci.org/MiserableNinja/Blossom)
=========
Blossom is a Gradle plugin that enables the ability perform source code token replacements in Java-based projects. It is licensed under the [LGPL v2.1].

## Usage
The example usage provided shows how to replace any values with the word 'APPLE' (case-sensitive) in the target file with the word 'BANANA'. 

To start with. You'll need to add the plugin to your plugins block.

```groovy
plugins {
    id "ninja.miserable.blossom" version "1.0.1"
}
```


#### Global Replacement (all files)
This example shows the usage to replace all instances of the world `APPLE` (case-sensitive) with the word `BANANA` in **all files**. This can be seen as `replaceToken 'REPLACE_THIS', 'WITH_THIS'`.

```groovy
blossom {
    replaceToken 'APPLE', 'BANANA'
}
```

#### Local Replacement (per-file)
This example shows the usage to replace all instances of the world `APPLE` (case-sensitive) with the word `BANANA` in the **specified file(s)**. This can be seen as `replaceToken 'REPLACE_THIS', 'WITH_THIS', 'IN_THIS_FILE'`.

```groovy
blossom {
    def constants = 'src/main/java/org/test/testy/McTesterConstants.java'
    replaceToken 'APPLE', 'BANANA', constants
}
```

[Gradle]: http://www.gradle.org
[LGPL v2.1]: https://choosealicense.com/licenses/lgpl-2.1/
