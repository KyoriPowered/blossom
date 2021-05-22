/*
 * This file is part of blossom, licensed under the GNU Lesser General Public License.
 *
 * Copyright (c) 2015-2016 MiserableNinja
 * Copyright (c) 2018-2021 KyoriPowered
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

import net.kyori.blossom.task.BuiltInSourceReplacementTasks;
import net.kyori.blossom.task.SourceReplacementTask;
import net.kyori.mammoth.ProjectPlugin;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.gradle.api.Project;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.plugins.Convention;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.compile.AbstractCompile;

import java.io.File;

/**
 * The blossom plugin.
 */
public final class Blossom implements ProjectPlugin {
  /**
   * The name of the blossom extension.
   */
  public static final String EXTENSION_NAME = "blossom";
  /**
   * The current project.
   */
  private @MonotonicNonNull Project project;

  @Override
  public void apply(
    final @NonNull Project project,
    final @NonNull PluginContainer plugins,
    final @NonNull ExtensionContainer extensions,
    final @NonNull Convention convention,
    final @NonNull TaskContainer tasks
  ) {
    this.project = project;

    project.afterEvaluate(p -> {
      final BlossomExtension extension = (BlossomExtension) extensions.getByName(EXTENSION_NAME);
      // Configure tasks with extension data
      tasks.withType(SourceReplacementTask.class, task -> {
        task.setTokenReplacementsGlobal(extension.getTokenReplacementsGlobal());
        task.setTokenReplacementsGlobalLocations(extension.getTokenReplacementsGlobalLocations());
        task.setTokenReplacementsByFile(extension.getTokenReplacementsByFile());
      });
    });
    extensions.create(EXTENSION_NAME, BlossomExtension.class, this);

    plugins.apply("java");
    this.setupSourceReplacementTasks();
  }

  private void setupSourceReplacementTasks() {
    final JavaPluginConvention javaPluginConvention = (JavaPluginConvention) this.project.getConvention().getPlugins().get("java");
    final SourceSet mainSourceSet = javaPluginConvention.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);

    BuiltInSourceReplacementTasks.setupJava(this, mainSourceSet);

    if(this.project.getPlugins().hasPlugin("scala")) {
      BuiltInSourceReplacementTasks.setupScala(this, mainSourceSet);
    }

    if(this.project.getPlugins().hasPlugin("groovy")) {
      BuiltInSourceReplacementTasks.setupGroovy(this, mainSourceSet);
    }
  }

  /**
   * Sets up a source replacement task.
   *
   * @param name            name for the task
   * @param inputSource     input sources
   * @param outputPath      output path
   * @param compileTaskName name of compile task
   */
  public void setupSourceReplacementTask(final String name, final SourceDirectorySet inputSource, final String outputPath, final String compileTaskName) {
    final File outputDirectory = new File(this.project.getBuildDir(), outputPath);

    this.project.getTasks().register(name, SourceReplacementTask.class, sourceReplacementTask -> {
      sourceReplacementTask.setInput(inputSource);
      sourceReplacementTask.setOutput(outputDirectory);
    });

    this.project.getTasks().named(compileTaskName, AbstractCompile.class, compileTask -> {
      compileTask.dependsOn(name);
      compileTask.setSource(outputDirectory);
    });
  }

  Project getProject() {
    return this.project;
  }
}
