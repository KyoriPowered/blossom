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
import net.kyori.blossom.test.BlossomDisplayNameGeneration;
import net.kyori.blossom.test.BlossomFunctionalTest;
import net.kyori.blossom.test.SettingsFactory;
import net.kyori.mammoth.test.TestContext;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.DisplayNameGeneration;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayNameGeneration(BlossomDisplayNameGeneration.class)
class SourceTemplateTest {
  @BlossomFunctionalTest
  void testSourceSingleSet(final TestContext ctx) throws IOException {
    SettingsFactory.writeSettings(ctx, "sourceSingleSet");
    ctx.copyInput("build.gradle");
    ctx.copyInput("Main.java", "src/main/java/test/Main.java");
    ctx.copyInput("BuildParameters.java.peb", "src/main/java-templates/test/BuildParameters.java.peb");

    // the java class Main.java reads a field from the generated template
    final BuildResult result = ctx.build("build");

    assertEquals(TaskOutcome.SUCCESS, result.task(":generateJavaTemplates").getOutcome());
  }

  @BlossomFunctionalTest
  void testSourceMultiVariant(final TestContext ctx) throws IOException {
    SettingsFactory.writeSettings(ctx, "sourceSingleSet");
    ctx.copyInput("build.gradle");
    ctx.copyInput("Main.java", "src/main/java/test/Main.java");
    ctx.copyInput("template-data.yaml");
    ctx.copyInput("{{ wrapper }}{{ type }}Box.java.peb", "src/main/java-templates/test/{{ wrapper }}{{ type }}Box.java.peb");

    // the java class Main.java reads a field from the generated template
    final BuildResult result = ctx.build("build");

    assertEquals(TaskOutcome.SUCCESS, result.task(":generateJavaTemplates").getOutcome());

  }
}
