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
import org.jspecify.annotations.NullMarked;

/**
 * A directory of templates.
 */
@NullMarked
public abstract class TemplateSetImpl implements TemplateSetInternal {
  // shared
  private final ConfigurableFileCollection dataFiles;
  private final MapProperty<String, Object> properties;
  private final NamedDomainObjectContainer<Variant> variants;
  private final Property<String> header;
  private final Property<Boolean> trimNewlines;
  private transient final SourceDirectorySet includes;
  private transient final SourceDirectorySet templates;
  private final String name;

  @Inject
  public TemplateSetImpl(final ObjectFactory objects, final String name) {
    this.name = name;
    this.dataFiles = objects.fileCollection();
    this.properties = objects.mapProperty(String.class, Object.class);
    this.variants = objects.domainObjectContainer(Variant.class, n -> objects.newInstance(VariantImpl.class, n));
    this.header = objects.property(String.class);
    this.trimNewlines = objects.property(Boolean.class).convention(true);
    this.includes = objects.sourceDirectorySet(name + "-template-includes", name + " template includes");
    this.templates = objects.sourceDirectorySet(name + "-templates", name + " templates");
  }

  @Override
  public String getName() {
    return this.name;
  }

  // global properties

  @Override
  public ConfigurableFileCollection getPropertyFiles() { // if there are variants, reads per-variant data from a list under `variants`
    return this.dataFiles;
  }

  @Override
  public MapProperty<String, Object> getProperties() {
    return this.properties;
  }

  @Override
  public Property<String> getHeader() {
    return this.header;
  }

  @Override
  public Property<Boolean> getTrimNewlines() {
    return this.trimNewlines;
  }

  @Override
  public SourceDirectorySet getIncludes() {
    return this.includes;
  }

  @Override
  public SourceDirectorySet getTemplates() {
    return this.templates;
  }

  // variant

  @Override
  public NamedDomainObjectContainer<Variant> getVariants() {
    return this.variants;
  }
}
