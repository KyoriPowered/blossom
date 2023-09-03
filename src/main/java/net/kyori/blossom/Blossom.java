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

import net.kyori.blossom.internal.BlossomExtensionImpl;
import net.kyori.blossom.internal.TemplateSetInternal;
import net.kyori.mammoth.ProjectPlugin;
import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.plugins.ide.eclipse.EclipsePlugin;
import org.gradle.plugins.ide.eclipse.model.EclipseModel;
import org.gradle.util.GradleVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A template processor for Gradle projects.
 *
 * <p>Uses pebble templates, properties are applied on a sourceset level.</p>
 *
 * @since 2.0.0
 */
public class Blossom implements ProjectPlugin {

  private static final String GENERATION_GROUP = "blossom";

  private static final String EXTENSION_NAME = "blossom";

  @Override
  public void apply(
    final @NotNull Project project,
    final @NotNull PluginContainer plugins,
    final @NotNull ExtensionContainer extensions,
    final @NotNull TaskContainer tasks
  ) {
    plugins.withType(JavaBasePlugin.class, $ -> {
      this.registerGenerateAllTask(plugins, extensions, tasks);

      final SourceSetContainer sourceSets = extensions.getByType(SourceSetContainer.class);
      sourceSets.all(set -> {
        final BlossomExtension extension = set.getExtensions().create(BlossomExtension.class, EXTENSION_NAME, BlossomExtensionImpl.class, project.getObjects());
        final Directory baseInputDir = project.getLayout().getProjectDirectory().dir("src/" + set.getName());
        final Provider<Directory> generatedBase = project.getLayout().getBuildDirectory().dir("generated");

        // generate a task for each template set
        extension.getTemplateSets().all(templateSet -> {
          final var internal = (TemplateSetInternal) templateSet;
          final Provider<Directory> templateSetOutput = generatedBase.map(internal::resolveOutputDirectory);
          internal.templates(baseInputDir.dir(templateSet.getName() + "-templates"));
          internal.getTemplates().getDestinationDirectory().set(templateSetOutput);
          final TaskProvider<GenerateTemplates> generateTask = tasks.register(set.getTaskName("generate", templateSet.getName() + "Templates"), GenerateTemplates.class, task -> {
            task.setGroup(Blossom.GENERATION_GROUP);
            task.getBaseSet().set(templateSet);
          });
          internal.getTemplates().compiledBy(generateTask, GenerateTemplates::getOutputDir);

          // And add the output as a source directory
          internal.registerOutputWithSet(set, generateTask);
        });
      });
    });
  }

  private void registerGenerateAllTask(final PluginContainer plugins, final ExtensionContainer extensions, final TaskContainer tasks) {
    final TaskProvider<?> generateTemplates = tasks.register("generateTemplates", task -> {
      task.dependsOn(tasks.withType(GenerateTemplates.class));
    });

    plugins.withType(EclipsePlugin.class, eclipse -> {
      extensions.getByType(EclipseModel.class).synchronizationTasks(generateTemplates);
    });

    plugins.withId("org.jetbrains.gradle.plugin.idea-ext", ideaExt -> {
      // todo
    });
  }

  @Override
  public @Nullable GradleVersion minimumGradleVersion() {
    return GradleVersion.version("7.2");
  }
}
