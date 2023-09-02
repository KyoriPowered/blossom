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
import net.kyori.blossom.ResourceTemplateSet;
import net.kyori.blossom.SourceSetTemplateExtension;
import net.kyori.blossom.TemplateSet;
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer;
import org.gradle.api.PolymorphicDomainObjectContainer;
import org.gradle.api.model.ObjectFactory;
import org.jetbrains.annotations.NotNull;

public class SourceSetTemplateExtensionImpl implements SourceSetTemplateExtension {
  private final ExtensiblePolymorphicDomainObjectContainer<TemplateSet> templateSets;

  @Inject
  public SourceSetTemplateExtensionImpl(final ObjectFactory objects) {
    this.templateSets = objects.polymorphicDomainObjectContainer(TemplateSet.class);
    this.templateSets.registerBinding(ResourceTemplateSet.class, ResourceTemplateSetImpl.class);
  }

  @Override
  public @NotNull PolymorphicDomainObjectContainer<TemplateSet> getTemplateSets() {
    return this.templateSets;
  }
}
