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
import org.gradle.api.tasks.GroovySourceSet;
import org.gradle.api.tasks.ScalaSourceSet;
import org.gradle.api.tasks.SourceSet;

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
    final ScalaSourceSet set = (ScalaSourceSet) new DslObject(mainSourceSet).getConvention().getPlugins().get("scala");
    blossom.setupSourceReplacementTask("blossomSourceReplacementScala", set.getScala(), "sources/scala/", mainSourceSet.getCompileTaskName("scala"));
  }

  /**
   * Setup the default Groovy source replacement task.
   *
   * @param blossom       blossom instance
   * @param mainSourceSet source set
   */
  static void setupGroovy(final Blossom blossom, final SourceSet mainSourceSet) {
    final GroovySourceSet set = (GroovySourceSet) new DslObject(mainSourceSet).getConvention().getPlugins().get("groovy");
    blossom.setupSourceReplacementTask("blossomSourceReplacementGroovy", set.getGroovy(), "sources/groovy/", mainSourceSet.getCompileTaskName("groovy"));
  }
}
