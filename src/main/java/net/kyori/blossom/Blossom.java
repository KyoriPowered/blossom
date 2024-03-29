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

import java.io.File;
import net.kyori.blossom.internal.BlossomExtensionImpl;
import net.kyori.blossom.internal.BuildParameters;
import net.kyori.blossom.internal.IdeConfigurer;
import net.kyori.blossom.internal.TemplateSetInternal;
import net.kyori.mammoth.ProjectPlugin;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.file.Directory;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.plugins.ide.eclipse.model.EclipseModel;
import org.gradle.plugins.ide.idea.model.IdeaModel;
import org.gradle.util.GradleVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.gradle.ext.ProjectSettings;
import org.jetbrains.gradle.ext.TaskTriggersConfig;

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
  private static final String BLOSSOM_RUNTIME_CONFIG = "blossomRuntime";
  private static final String PEBBLE_ARTIFACT_ID = "io.pebbletemplates:pebble";
  private static final String SNAKEYAML_ARTIFACT_ID = "org.snakeyaml:snakeyaml-engine";

  @Override
  public void apply(
    final @NotNull Project project,
    final @NotNull PluginContainer plugins,
    final @NotNull ExtensionContainer extensions,
    final @NotNull TaskContainer tasks
  ) {
    plugins.withType(JavaBasePlugin.class, $ -> {
      final SetProperty<File> outputDirs = project.getObjects().setProperty(File.class);
      this.registerGenerateAllTask(project, tasks, outputDirs);

      final SourceSetContainer sourceSets = extensions.getByType(SourceSetContainer.class);
      final NamedDomainObjectProvider<Configuration> blossomRuntimeConfig = this.registerBlossomRuntimeConfig(project.getDependencies(), project.getConfigurations());
      sourceSets.all(set -> {
        final BlossomExtension extension = set.getExtensions().create(BlossomExtension.class, EXTENSION_NAME, BlossomExtensionImpl.class, project.getObjects());
        final Directory baseInputDir = project.getLayout().getProjectDirectory().dir("src/" + set.getName());
        final Provider<Directory> generatedBase = project.getLayout().getBuildDirectory().dir("generated");

        // generate a task for each template set
        extension.getTemplateSets().all(templateSet -> {
          final var internal = (TemplateSetInternal) templateSet;
          final Provider<Directory> templateSetOutput = generatedBase.map(internal::resolveOutputRoot).map(dir -> dir.dir("blossom/" + set.getName() + "/" + templateSet.getName()));
          internal.templates(baseInputDir.dir(templateSet.getName() + "-templates"));
          internal.getTemplates().getDestinationDirectory().set(templateSetOutput);
          final TaskProvider<GenerateTemplates> generateTask = tasks.register(set.getTaskName("generate", templateSet.getName() + "Templates"), GenerateTemplates.class, task -> {
            task.setGroup(Blossom.GENERATION_GROUP);
            task.getBaseSet().set(templateSet);
            task.getPebbleClasspath().from(blossomRuntimeConfig.map(it -> it.getIncoming().getFiles()));
          });
          outputDirs.add(internal.getTemplates().getDestinationDirectory().map(Directory::getAsFile));
          internal.getTemplates().compiledBy(generateTask, GenerateTemplates::getOutputDir);

          // And add the output as a source directory
          internal.registerOutputWithSet(set, generateTask);
        });
      });
    });
  }

  private NamedDomainObjectProvider<Configuration> registerBlossomRuntimeConfig(final DependencyHandler dependencies, final ConfigurationContainer configurations) {
    return configurations.register(BLOSSOM_RUNTIME_CONFIG, config -> {
      config.setDescription("Dependencies used to perform template processing with Blossom. Currently includes Pebble and SnakeYAML Engine");
      config.setVisible(false);
      config.setCanBeConsumed(false);
      config.defaultDependencies(deps -> {
        deps.add(dependencies.create(SNAKEYAML_ARTIFACT_ID + ':' + BuildParameters.SNAKEYAML_VERSION));
        deps.add(dependencies.create(PEBBLE_ARTIFACT_ID + ':' + BuildParameters.PEBBLE_VERSION));
      });
    });
  }

  private void registerGenerateAllTask(final Project project, final TaskContainer tasks, final SetProperty<File> outputDirs) {
    final TaskProvider<?> generateTemplates = tasks.register("generateTemplates", task -> {
      task.dependsOn(tasks.withType(GenerateTemplates.class));
    });

    IdeConfigurer.apply(project, new IdeConfigurer.IdeImportAction() {
      @Override
      public void idea(final @NotNull Project project, final @NotNull IdeaModel idea, final @NotNull ProjectSettings ideaExtension) {
        ((ExtensionAware) ideaExtension).getExtensions().getByType(TaskTriggersConfig.class).afterSync(generateTemplates);
        project.afterEvaluate(p -> {
          final @Nullable IdeaModel projectIdea = p.getExtensions().getByType(IdeaModel.class);
          if (projectIdea.getModule() != null) {
            projectIdea.getModule().getGeneratedSourceDirs().addAll(outputDirs.get());
          }
        });
      }

      @Override
      public void eclipse(final @NotNull Project project, final @NotNull EclipseModel eclipse) {
        eclipse.synchronizationTasks(generateTemplates);
      }
    });
  }

  @Override
  public @Nullable GradleVersion minimumGradleVersion() {
    return GradleVersion.version("7.2");
  }
}
