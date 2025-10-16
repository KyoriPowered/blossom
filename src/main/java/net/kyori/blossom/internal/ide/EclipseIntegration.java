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
package net.kyori.blossom.internal.ide;

import java.util.function.Consumer;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.plugins.ide.eclipse.EclipsePlugin;
import org.gradle.plugins.ide.eclipse.model.EclipseModel;

public final class EclipseIntegration {

  private EclipseIntegration() {
  }

  /**
   * Applies the specified configuration action to configure Eclipse projects.
   *
   * <p>This does not apply the Eclipse plugin, but will perform the action when the plugin is applied.</p>
   *
   * @param project project to apply to
   * @param action  the action to perform
   */
  public static void apply(final Project project, final Consumer<EclipseModel> action) {
    project.getPlugins().withType(EclipsePlugin.class, plugin -> {
      final EclipseModel model = project.getExtensions().findByType(EclipseModel.class);
      if (model == null) {
        return;
      }
      action.accept(model);
    });
  }

  /**
   * Executes a task when Eclipse performs a project synchronization.
   *
   * @param project project of the task
   * @param task    the task to perform on synchronization
   */
  public static void addSynchronizationTask(final Project project, final TaskProvider<?> task) {
    EclipseIntegration.apply(project, (eclipseModel -> eclipseModel.synchronizationTasks(task)));
  }
}
