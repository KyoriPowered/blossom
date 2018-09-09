/*
 * This file is part of Blossom, licensed under the GNU Lesser General Public License.
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
import net.kyori.blossom.task.SourceReplacementTask;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.logging.Logger;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.GroovySourceSet;
import org.gradle.api.tasks.ScalaSourceSet;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.compile.GroovyCompile;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.scala.ScalaCompile;

import java.io.File;

public final class Blossom implements Plugin<Project> {

    public static final String EXTENSION_NAME = "blossom";
    /** A flag to prevent the banned from being displayed more than one time. */
    private static boolean displayBanner = true;
    /** The current project. */
    public Project project;

    @Override
    public void apply(Project projectIn) {
        this.project = projectIn;

        this.project.afterEvaluate(new Action<Project>() {
            @Override
            public void execute(Project project) {
                if (displayBanner) {
                    Logger logger = Blossom.this.project.getLogger();
                    logger.lifecycle("########################");
                    logger.lifecycle("## Powered by Blossom ##");
                    logger.lifecycle("########################");
                }

                displayBanner = false;

                final BlossomExtension extension = (BlossomExtension) Blossom.this.project.getExtensions().getByName(EXTENSION_NAME);
                // Configure tasks with extension data
                for (final SourceReplacementTask task : Blossom.this.project.getTasks().withType(SourceReplacementTask.class)) {
                    task.setTokenReplacementsGlobal(extension.getTokenReplacementsGlobal());
                    task.setTokenReplacementsGlobalLocations(extension.getTokenReplacementsGlobalLocations());
                    task.setTokenReplacementsByFile(extension.getTokenReplacementsByFile());
                }
            }
        });

        this.project.getExtensions().create(EXTENSION_NAME, BlossomExtension.class, this);

        this.applyPlugin("java");
        this.createTasks();
    }

    private void createTasks() {
        this.createSourceReplacementTasks();
    }

    private void createSourceReplacementTasks() {
        final JavaPluginConvention javaPluginConvention = (JavaPluginConvention) this.project.getConvention().getPlugins().get("java");
        final SourceSet main = javaPluginConvention.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);

        File dir;
        SourceReplacementTask task;

        // Java
        {
            dir = new File(this.project.getBuildDir(), "sources/java/");
            task = this.makeTask("blossomSourceReplacementJava", SourceReplacementTask.class);
            task.setInput(main.getJava());
            task.setOutput(dir);

            JavaCompile compile = (JavaCompile) this.project.getTasks().getByName(main.getCompileJavaTaskName());
            compile.dependsOn("blossomSourceReplacementJava");
            compile.setSource(dir);
        }

        // Scala
        if (this.project.getPlugins().hasPlugin("scala")) {
            dir = new File(this.project.getBuildDir(), "sources/scala/");
            ScalaSourceSet set = (ScalaSourceSet) new DslObject(main).getConvention().getPlugins().get("scala");

            task = this.makeTask("blossomSourceReplacementScala", SourceReplacementTask.class);
            task.setInput(set.getScala());
            task.setOutput(dir);

            ScalaCompile compile = (ScalaCompile) this.project.getTasks().getByName(main.getCompileTaskName("scala"));
            compile.dependsOn("blossomSourceReplacementScala");
            compile.setSource(dir);
        }

        // Groovy
        if (this.project.getPlugins().hasPlugin("groovy")) {
            dir = new File(this.project.getBuildDir(), "sources/groovy/");
            GroovySourceSet set = (GroovySourceSet) new DslObject(main).getConvention().getPlugins().get("groovy");

            task = this.makeTask("blossomSourceReplacementGroovy", SourceReplacementTask.class);
            task.setInput(set.getGroovy());
            task.setOutput(dir);

            GroovyCompile compile = (GroovyCompile) this.project.getTasks().getByName(main.getCompileTaskName("groovy"));
            compile.dependsOn("blossomSourceReplacementGroovy");
            compile.setSource(dir);
        }
    }

    private void applyPlugin(String plugin) {
        this.project.apply(ImmutableMap.of("plugin", plugin));
    }

    /**
     * Create a task.
     *
     * @param name The task name
     * @param type The task class
     * @param <T> The task type
     * @return The task
     */
    private <T extends Task> T makeTask(final String name, final Class<T> type) {
        return (T) this.project.task(ImmutableMap.of("name", name, "type", type), name);
    }
}
