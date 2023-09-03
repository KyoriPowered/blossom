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

import javax.inject.Inject;
import net.kyori.blossom.Variant;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.jetbrains.annotations.NotNull;

/**
 * A directory of templates.
 *
 * <p>While it's perfectly possible to </p>
 */
public abstract class TemplateSetImpl implements TemplateSetInternal {
  // shared
  private final ConfigurableFileCollection dataFiles;
  private final MapProperty<String, Object> properties;
  private final NamedDomainObjectContainer<Variant> variants;
  private final Property<String> header;
  private transient final SourceDirectorySet includes;
  private transient final SourceDirectorySet templates;
  private final String name;

  @Inject
  public TemplateSetImpl(final String name) {
    this.name = name;
    this.dataFiles = this.getObjects().fileCollection();
    this.properties = this.getObjects().mapProperty(String.class, Object.class);
    this.variants = this.getObjects().domainObjectContainer(Variant.class, n -> this.getObjects().newInstance(VariantImpl.class, n));
    this.header = this.getObjects().property(String.class);
    this.includes = this.getObjects().sourceDirectorySet(name + "-template-includes", name + " template includes");
    this.templates = this.getObjects().sourceDirectorySet(name + "-templates", name + " templates");
  }

  @Inject
  protected abstract ObjectFactory getObjects();

  @Override
  public @NotNull String getName() {
    return this.name;
  }

  // global properties

  @Override
  public @NotNull ConfigurableFileCollection getPropertyFiles() { // if there are variants, reads per-variant data from a list under `variants`
    return this.dataFiles;
  }

  @Override
  public @NotNull MapProperty<String, Object> getProperties() {
    return this.properties;
  }

  @Override
  public @NotNull Property<String> getHeader() {
    return this.header;
  }

  @Override
  public @NotNull SourceDirectorySet getIncludes() {
    return this.includes;
  }

  @Override
  public @NotNull SourceDirectorySet getTemplates() {
    return this.templates;
  }

  // variant

  @Override
  public @NotNull NamedDomainObjectContainer<Variant> getVariants() {
    return this.variants;
  }
}
