/*
 * This file is part of blossom, licensed under the GNU Lesser General Public License.
 *
 * Copyright (c) 2023 KyoriPowered
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package net.kyori.blossom;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import net.kyori.blossom.test.BlossomDisplayNameGeneration;
import net.kyori.blossom.test.BlossomFunctionalTest;
import net.kyori.blossom.test.SettingsFactory;
import net.kyori.mammoth.test.TestContext;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayNameGeneration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled
@DisplayNameGeneration(BlossomDisplayNameGeneration.class)
class IncludedMacrosTest {
  @BlossomFunctionalTest
  void testIncludedMacros(final TestContext ctx) throws IOException {
    SettingsFactory.writeSettings(ctx, "includedMacros");
    ctx.copyInput("build.gradle");
    ctx.copyInput("test.properties.peb", "src/main/resource-templates/test.properties.peb");
    ctx.copyInput("macros.peb", "src/main/resource-macros/macros.peb");

    final BuildResult result = ctx.build("assemble"); // build a jar

    assertEquals(TaskOutcome.SUCCESS, result.task(":generateResourceTemplates").getOutcome());

    final var destPath = ctx.outputDirectory().resolve("build/libs/includedMacros.jar");
    assertTrue(Files.isRegularFile(destPath), "The expected jar did not exist");

    try (final var jar = new JarFile(destPath.toFile())) {
      final JarEntry entry = jar.getJarEntry("test.properties");
      assertNotNull(entry, "no test.properties in jar");
      final Properties props = new Properties();
      try (final InputStream is = jar.getInputStream(entry)) {
        props.load(is);
      }

      assertEquals("abc123 abc123 abc123", props.getProperty("value"));
    }
  }
}
