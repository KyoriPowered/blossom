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

import javax.inject.Inject;
import net.kyori.mammoth.Configurable;
import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.jetbrains.annotations.NotNull;

/**
 * Template variant.
 *
 * <p>Composed of:</p>
 * <ul>
 *     <li>Name: not used for anything</li>
 *     <li>Source files: files that will be loaded</li>
 *     <li>Runtime properties: set in the buildscript, will override anything
 *     set in source files</li>
 * </ul>
 *
 * @since 2.0.0
 */
public class Variant implements Named {
  private final String name;
  private final ConfigurableFileCollection sourceFiles;
  private final MapProperty<String, Object> runtimeProperties;

  /**
   * Create a new variant (not to be used directly).
   *
   * @param name variant name
   * @param objects injected
   * @since 2.0.0
   */
  @Inject
  public Variant(final String name, final ObjectFactory objects) {
    this.name = name;
    this.sourceFiles = objects.fileCollection();
    this.runtimeProperties = objects.mapProperty(String.class, Object.class);
  }

  @Override
  @Input
  public @NotNull String getName() {
    return this.name;
  }

  /**
   * Data files containing template parameters.
   *
   * @return the data files to read
   * @since 2.0.0
   */
  @InputFiles
  public @NotNull ConfigurableFileCollection getDataFiles() {
    return this.sourceFiles;
  }

  /**
   * Runtime properties for inserting into templates.
   *
   * @return the properties map
   * @since 2.0.0
   */
  @Input
  public @NotNull MapProperty<String, Object> getProperties() {
    return this.runtimeProperties;
  }

  /**
   * Register multiple properties for this variant.
   *
   * @param configureAction action to configure properties
   * @since 2.0.0
   */
  public void properties(final @NotNull Action<MapProperty<String, Object>> configureAction) {
    Configurable.configure(this.runtimeProperties, configureAction);
  }
}
