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

import net.kyori.mammoth.Configurable;
import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Template variant.
 *
 * <p>Composed of:</p>
 * <ul>
 *     <li>Name: used to map values from properties file</li>
 *     <li>Source files: files that will be loaded</li>
 *     <li>Runtime properties: set in the buildscript, will override anything
 *     set in source files</li>
 * </ul>
 *
 * @since 2.0.0
 */
@ApiStatus.NonExtendable
public interface Variant extends Named {
  @Override
  @Input
  @NotNull String getName();

  /**
   * Data files containing template parameters.
   *
   * @return the data files to read
   * @since 2.0.0
   */
  @InputFiles
  @NotNull ConfigurableFileCollection getDataFiles();

  /**
   * Runtime properties for inserting into templates.
   *
   * @return the properties map
   * @since 2.0.0
   */
  @Input
  @NotNull MapProperty<String, Object> getProperties();

  /**
   * Register multiple properties for this variant.
   *
   * @param configureAction action to configure properties
   * @since 2.0.0
   */
  default void properties(final @NotNull Action<MapProperty<String, Object>> configureAction) {
    Configurable.configure(this.getProperties(), configureAction);
  }
}
