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
package net.kyori.blossom.internal;

import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.plugins.ide.eclipse.EclipsePlugin;
import org.gradle.plugins.ide.eclipse.model.EclipseModel;
import org.gradle.plugins.ide.idea.model.IdeaModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.gradle.ext.IdeaExtPlugin;
import org.jetbrains.gradle.ext.ProjectSettings;

/**
 * Configures different IDEs when applicable.
 */
public final class IdeConfigurer {
  private static final String IDEA_PLUGIN = "org.jetbrains.gradle.plugin.idea-ext";

  private IdeConfigurer() {
  }

  /**
   * Get whether Gradle is being invoked through IntelliJ IDEA.
   *
   * <p>This can be through a project import, or a task execution.</p>
   *
   * @return whether this is an IntelliJ-based invocation
   */
  public static boolean isIdeaImport() {
    return Boolean.getBoolean("idea.active");
  }

  /**
   * Get whether this Gradle invocation is from an Eclipse project import.
   *
   * @return whether an eclipse import is ongoing
   */
  public static boolean isEclipseImport() {
    return System.getProperty("eclipse.application") != null;
  }

  /**
   * Applies the specified configuration action to configure IDE projects.
   *
   * <p>This does not apply the IDEs' respective plugins, but will perform
   * actions when those plugins are applied.</p>
   *
   * @param project project to apply to
   * @param toPerform the actions to perform
   */
  public static void apply(final @NotNull Project project, final @NotNull IdeImportAction toPerform) {
    project.getPlugins().withId(IDEA_PLUGIN, plugin -> {
      if (!IdeConfigurer.isIdeaImport()) {
        return;
      }

      applyIdea(project, toPerform);
    });
    project.getPlugins().withType(EclipsePlugin.class, plugin -> {
      final EclipseModel model = project.getExtensions().findByType(EclipseModel.class);
      if (model == null) {
        return;
      }
      toPerform.eclipse(project, model);
    });
  }

  private static void applyIdea(final Project project, final IdeImportAction toPerform) {
    // Apply the IDE plugin to the root project
    final Project rootProject = project.getRootProject();
    if (project != rootProject) {
      rootProject.getPlugins().apply(IdeaExtPlugin.class);
    }
    final IdeaModel model = rootProject.getExtensions().findByType(IdeaModel.class);
    if (model == null || model.getProject() == null) {
      return;
    }
    final ProjectSettings ideaExt = ((ExtensionAware) model.getProject()).getExtensions().getByType(ProjectSettings.class);

    // But actually perform the configuration with the subproject context
    toPerform.idea(project, model, ideaExt);
  }

  public interface IdeImportAction {
    /**
     * Configure an IntelliJ project.
     *
     * @param project the project to configure on import
     * @param idea the basic idea gradle extension
     * @param ideaExtension JetBrain's extensions to the base idea model
     */
    void idea(final @NotNull Project project, final @NotNull IdeaModel idea, final @NotNull ProjectSettings ideaExtension);

    /**
     * Configure an eclipse project.
     *
     * @param project the project being imported
     * @param eclipse the eclipse project model to modify
     */
    void eclipse(final @NotNull Project project, final @NotNull EclipseModel eclipse);
  }
}
