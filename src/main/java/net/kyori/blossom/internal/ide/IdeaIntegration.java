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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import org.gradle.StartParameter;
import org.gradle.TaskExecutionRequest;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.internal.DefaultTaskExecutionRequest;
import org.gradle.plugins.ide.idea.IdeaPlugin;
import org.gradle.plugins.ide.idea.model.IdeaModel;

public final class IdeaIntegration {

  private IdeaIntegration() {
  }

  /**
   * Get whether Gradle is being invoked through IntelliJ IDEA.
   *
   * <p>This can be through a project import, or a task execution.</p>
   *
   * @return whether this is an IntelliJ-based invocation
   */
  public static boolean isIdea() {
    return Boolean.getBoolean("idea.active");
  }

  /**
   * Get whether Gradle is being invoked through IntelliJ IDEA project synchronization.
   *
   * @return whether this is an IntelliJ-based synchronization
   */
  public static boolean isIdeaSync() {
    return Boolean.getBoolean("idea.sync.active");
  }

  /**
   * Applies the specified configuration action to configure Idea projects.
   *
   * <p>This does not apply the Idea plugin, but will perform the action when the plugin is applied.</p>
   *
   * @param project project to apply to
   * @param action  the action to perform
   */
  public static void apply(final Project project, final Consumer<IdeaModel> action) {
    project.getPlugins().withType(IdeaPlugin.class, plugin -> {
      if (!IdeaIntegration.isIdea()) {
        return;
      }
      final IdeaModel model = project.getExtensions().findByType(IdeaModel.class);
      if (model == null || model.getProject() == null) {
        return;
      }
      action.accept(model);
    });
  }

  /**
   * Executes a task when Idea performs a project synchronization.
   *
   * @param project project of the task
   * @param task    the task to perform on synchronization
   */
  public static void addSynchronizationTask(final Project project, final TaskProvider<?> task) {
    if (!IdeaIntegration.isIdeaSync()) {
      return;
    }

    project.afterEvaluate(p -> {
      final StartParameter startParameter = project.getGradle().getStartParameter();
      final List<TaskExecutionRequest> taskRequests = new ArrayList<>(startParameter.getTaskRequests());

      taskRequests.add(new DefaultTaskExecutionRequest(Collections.singletonList(":" + project.getName() + ":" + task.getName())));
      startParameter.setTaskRequests(taskRequests);
    });
  }
}
