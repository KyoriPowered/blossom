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
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.NamedDomainObjectSet;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.Optional;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * A set of templates associated with one source sets.
 *
 * <p>A template set can represent source or resource templates, including source templates for different languages.</p>
 *
 * @since 2.0.0
 */
@ApiStatus.NonExtendable
public interface TemplateSet extends Named {
  @Internal
  @Override
  @NotNull String getName();

  /**
   * A collection of data files in YAML format.
   *
   * @return the data file collection
   * @since 2.0.0
   */
  @InputFiles
  @NotNull ConfigurableFileCollection getDataFiles();

  /**
   * Add a data file for variable data.
   *
   * @param dataFile the data file to add, evaluated as per {@link org.gradle.api.Project#file(Object)}
   * @since 2.0.0
   */
  default void dataFile(final @NotNull Object dataFile) {
    this.getDataFiles().from(dataFile);
  }

  /**
   * Runtime-defined properties.
   *
   * <p>These properties will override anything in the data files.</p>
   *
   * @return the properties
   * @since 2.0.0
   */
  @Input
  @NotNull MapProperty<String, Object> getProperties();

  /**
   * Set a single property for this template.
   *
   * @param property the property
   * @param value the value for the property
   * @since 2.0.0
   */
  default void property(final String property, final String value) {
    this.getProperties().put(property, value);
  }

  /**
   * Set a single property for this template.
   *
   * @param property the property
   * @param value the provider providing a value for the property
   * @since 2.0.0
   */
  default void property(final String property, final Provider<String> value) {
    this.getProperties().put(property, value);
  }

  /**
   * A literal header to insert at the top of generated source files.
   *
   * <p>This property is optional.</p>
   *
   * @return the header
   * @since 2.0.0
   */
  @Input
  @Optional
  @NotNull Property<String> getHeader();

  /**
   * A container of template variants.
   *
   * <p>Variants will process the same templates, but with different property values.</p>
   *
   * @return the variants collection
   * @since 2.0.0
   */
  @Nested
  @NotNull NamedDomainObjectContainer<Variant> getVariants();

  /**
   * Register variants with certain names to be produced.
   *
   * @param variants the variants to produce
   * @since 2.0.0
   */
  default void variants(final @NotNull String@NotNull... variants) {
    for (final String variant : variants) {
      this.getVariants().register(variant);
    }
  }

  /**
   * Configure produced variants.
   *
   * @param configureAction an action to configure variants
   * @since 2.0.0
   */
  default void variants(final @NotNull Action<NamedDomainObjectSet<Variant>> configureAction) {
    Configurable.configure(this.getVariants(), configureAction);
  }

  /**
   * Directories providing templates which can be included into process templates, but which will not be processed for output themselves.
   *
   * @return the includes directories
   * @since 2.0.0
   */
  @Internal
  @NotNull SourceDirectorySet getIncludes();

  /**
   * Add includes directories to the template path.
   *
   * @param includes the includes directories to add, processed as in {@link org.gradle.api.Project#files(Object...)}
   * @see #getIncludes()
   * @since 2.0.0
   */
  default void include(final @NotNull Object@NotNull... includes) {
    this.getIncludes().srcDirs(includes);
  }
}
