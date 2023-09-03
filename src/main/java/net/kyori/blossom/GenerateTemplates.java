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

import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Inject;
import net.kyori.blossom.internal.worker.GenerateWorker;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SkipWhenEmpty;
import org.gradle.api.tasks.TaskAction;
import org.gradle.workers.WorkerExecutor;
import org.jetbrains.annotations.NotNull;

/**
 * Generate real files based on templates and input parameters.
 *
 * @since 2.0.0
 */
public abstract class GenerateTemplates extends DefaultTask {

  /**
   * The set the template is generated from.
   *
   * @return the template set
   * @since 2.0.0
   */
  @Nested
  public abstract @NotNull Property<TemplateSet> getBaseSet();

  /**
   * Files that can be included in templates, but that are not themselves templates.
   *
   * <p>Derived from the TemplateSet.</p>
   *
   * @return the files to include
   * @since 2.0.0
   */
  @InputFiles
  protected abstract @NotNull ConfigurableFileCollection getIncludesDirectories();

  /**
   * Source directory for templates to process.
   *
   * @return the source directory
   * @since 2.0.0
   */
  @InputFiles
  @SkipWhenEmpty
  protected abstract @NotNull ConfigurableFileCollection getSourceDirectories();

  /**
   * Destination directory for template output.
   *
   * @return the destination directory
   * @since 2.0.0
   */
  @OutputDirectory
  public abstract @NotNull DirectoryProperty getOutputDir();

  /**
   * The worker classpath. This should include Pebble and SnakeYAML engine.
   *
   * @return the worker classpath
   * @since 2.0.0
   */
  @Classpath
  public abstract ConfigurableFileCollection getPebbleClasspath();

  /**
   * Create a new task (NOT to be called directly).
   *
   * @since 2.0.0
   */
  public GenerateTemplates() {
    this.getIncludesDirectories().from(this.getBaseSet().map(set -> set.getIncludes().getSourceDirectories()));
    this.getSourceDirectories().from(this.getBaseSet().map(set -> set.getTemplates().getSourceDirectories()));
  }

  @Inject
  protected abstract WorkerExecutor getWorkerExecutor();

  @TaskAction
  void generate() {
    this.getWorkerExecutor().classLoaderIsolation(spec -> {
      spec.getClasspath().from(this.getPebbleClasspath());
    }).submit(GenerateWorker.class, spec -> {
      // global params
      spec.getSetName().set(this.getBaseSet().map(TemplateSet::getName));
      spec.getGlobalParameters().set(this.getBaseSet().flatMap(TemplateSet::getProperties));
      spec.getGlobalParameterFiles().from(this.getBaseSet().map(TemplateSet::getDataFiles));

      // variant parameters
      spec.getVariantParameters().set(this.getBaseSet().map(set ->
        set.getVariants().getAsMap().entrySet()
        .stream()
        .collect(Collectors.toMap(Map.Entry::getKey, ent -> ent.getValue().getProperties().getOrElse(Map.of())))
      ));
      // variant parameter files
      spec.getVariantFiles().set(this.getBaseSet().map(set ->
        set.getVariants().getAsMap().entrySet()
          .stream()
          .collect(Collectors.toMap(Map.Entry::getKey, ent -> ent.getValue().getDataFiles()))
      ));

      // general properties
      spec.getHeader().set(this.getBaseSet().flatMap(TemplateSet::getHeader));
      spec.getSourceDirectories().from(this.getSourceDirectories());
      spec.getIncludesDirectories().from(this.getIncludesDirectories());
      spec.getDestinationDirectory().set(this.getOutputDir());
    });
  }

}
