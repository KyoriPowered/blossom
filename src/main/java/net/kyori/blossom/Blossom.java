/*
 * This file is part of blossom, licensed under the GNU Lesser General Public License.
 *
 * Copyright (c) 2015-2016 MiserableNinja
 * Copyright (c) 2018 KyoriPowered
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

import com.google.common.collect.ImmutableMap;
import net.kyori.blossom.task.BuiltInSourceReplacementTasks;
import net.kyori.blossom.task.SourceReplacementTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.compile.AbstractCompile;

import java.io.File;

public final class Blossom implements Plugin<Project> {
  /** The name of the blossom extension */
  public static final String EXTENSION_NAME = "blossom";
  /** The current project. */
  private Project project;

  @Override
  public void apply(final Project project) {
    this.project = project;

    project.afterEvaluate(p -> {
      final BlossomExtension extension = (BlossomExtension) p.getExtensions().getByName(EXTENSION_NAME);
      // Configure tasks with extension data
      for(final SourceReplacementTask task : p.getTasks().withType(SourceReplacementTask.class)) {
        task.setTokenReplacementsGlobal(extension.getTokenReplacementsGlobal());
        task.setTokenReplacementsGlobalLocations(extension.getTokenReplacementsGlobalLocations());
        task.setTokenReplacementsByFile(extension.getTokenReplacementsByFile());
      }
    });
    project.getExtensions().create(EXTENSION_NAME, BlossomExtension.class, this);

    this.applyPlugin("java");
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

  public void setupSourceReplacementTask(final String name, final SourceDirectorySet inputSource, final String outputPath, final String compileTaskName) {
    final File outputDirectory = new File(this.project.getBuildDir(), outputPath);

    final SourceReplacementTask replacementTask = this.makeTask(name, SourceReplacementTask.class);
    replacementTask.setInput(inputSource);
    replacementTask.setOutput(outputDirectory);

    final AbstractCompile compileTask = (AbstractCompile) this.project.getTasks().getByName(compileTaskName);
    compileTask.dependsOn(name);
    compileTask.setSource(outputDirectory);
  }

  Project getProject() {
    return this.project;
  }

  private void applyPlugin(final String plugin) {
    this.project.apply(ImmutableMap.of("plugin", plugin));
  }

  private <T extends Task> T makeTask(final String name, final Class<T> type) {
    return (T) this.project.task(ImmutableMap.of("name", name, "type", type), name);
  }
}
