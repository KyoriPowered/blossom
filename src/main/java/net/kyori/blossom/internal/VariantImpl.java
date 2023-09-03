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
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.jetbrains.annotations.NotNull;

public class VariantImpl implements Variant {
  private final String name;
  private final ConfigurableFileCollection sourceFiles;
  private final MapProperty<String, Object> runtimeProperties;

  @Inject
  public VariantImpl(final String name, final ObjectFactory objects) {
    this.name = name;
    this.sourceFiles = objects.fileCollection();
    this.runtimeProperties = objects.mapProperty(String.class, Object.class);
  }

  @Override
  public @NotNull String getName() {
    return this.name;
  }

  @Override
  @InputFiles
  public @NotNull ConfigurableFileCollection getDataFiles() {
    return this.sourceFiles;
  }

  @Override
  @Input
  public @NotNull MapProperty<String, Object> getProperties() {
    return this.runtimeProperties;
  }
}
