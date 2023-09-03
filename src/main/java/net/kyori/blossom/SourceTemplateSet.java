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

import org.jetbrains.annotations.NotNull;

/**
 * A template set for sources.
 *
 * @since 2.0.0
 */
public interface SourceTemplateSet extends TemplateSet {
  /**
   * Configure to generate templates contributing to Groovy sources.
   *
   * @since 2.0.0
   */
  default void groovy() {
    this.namedLanguageExtension("groovy");
  }

  /**
   * Configure to generate templates contributing to Java sources.
   *
   * @since 2.0.0
   */
  void java();

  /**
   * Configure to generate templates contributing to Kotlin sources.
   *
   * @since 2.0.0
   */
  default void kotlin() {
    this.namedLanguageExtension("kotlin");
  }

  /**
   * Configure to generate templates contributing to Scala sources.
   *
   * @since 2.0.0
   */
  default void scala() {
    this.namedLanguageExtension("scala");
  }

  /**
   * Configure this template set to attach itself to a named {@link org.gradle.api.file.SourceDirectorySet}-typed extension on the target {@link org.gradle.api.tasks.SourceSet}.
   *
   * @param name the name of the language extension
   * @since 2.0.0
   */
  void namedLanguageExtension(final @NotNull String name);
}
