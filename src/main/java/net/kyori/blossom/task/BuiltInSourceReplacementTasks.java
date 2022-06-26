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
package net.kyori.blossom.task;

import net.kyori.blossom.Blossom;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.tasks.GroovySourceDirectorySet;
import org.gradle.api.tasks.ScalaSourceDirectorySet;
import org.gradle.api.tasks.SourceSet;
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet;

/**
 * Interface for registering the built in source replacement tasks.
 */
public interface BuiltInSourceReplacementTasks {
  /**
   * Setup the default Java source replacement task.
   *
   * @param blossom       blossom instance
   * @param mainSourceSet source set
   */
  static void setupJava(final Blossom blossom, final SourceSet mainSourceSet) {
    blossom.setupSourceReplacementTask("blossomSourceReplacementJava", mainSourceSet.getJava(), "sources/java/", mainSourceSet.getCompileJavaTaskName());
  }

  /**
   * Setup the default Scala source replacement task.
   *
   * @param blossom       blossom instance
   * @param mainSourceSet source set
   */
  static void setupScala(final Blossom blossom, final SourceSet mainSourceSet) {
    final ScalaSourceDirectorySet set = mainSourceSet.getExtensions().getByType(ScalaSourceDirectorySet.class);
    blossom.setupSourceReplacementTask("blossomSourceReplacementScala", set, "sources/scala/", mainSourceSet.getCompileTaskName("scala"));
  }

  /**
   * Setup the default Groovy source replacement task.
   *
   * @param blossom       blossom instance
   * @param mainSourceSet source set
   */
  static void setupGroovy(final Blossom blossom, final SourceSet mainSourceSet) {
    final GroovySourceDirectorySet set = mainSourceSet.getExtensions().getByType(GroovySourceDirectorySet.class);
    blossom.setupSourceReplacementTask("blossomSourceReplacementGroovy", set, "sources/groovy/", mainSourceSet.getCompileTaskName("groovy"));
  }

  /**
   * Setup the default Kotlin source replacement task.
   *
   * @param blossom       blossom instance
   * @param mainSourceSet source set
   */
  static void setupKotlin(final Blossom blossom, final SourceSet mainSourceSet) {
    final KotlinSourceSet set = (KotlinSourceSet) new DslObject(mainSourceSet).getConvention().getPlugins().get("kotlin");
    blossom.setupSourceReplacementTask("blossomSourceReplacementKotlin", set.getKotlin(), "sources/kotlin/", mainSourceSet.getCompileTaskName("kotlin"));
  }
}
