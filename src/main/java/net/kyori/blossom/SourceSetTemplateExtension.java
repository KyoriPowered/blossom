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

import java.util.Objects;
import net.kyori.mammoth.Configurable;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.PolymorphicDomainObjectContainer;
import org.jetbrains.annotations.NotNull;

/**
 * Templating that applies to a specific source set.
 *
 * @since 2.0.0
 */
public interface SourceSetTemplateExtension {
  String RESOURCE_TEMPLATE_SET_NAME = "resource";

  /**
   * Register a single primary template set for resource templates.
   *
   * @return the resource template set for configuration
   * @since 2.0.0
   */
  default @NotNull NamedDomainObjectProvider<ResourceTemplateSet> resources() {
    if (this.getTemplateSets().getNames().contains(RESOURCE_TEMPLATE_SET_NAME)) {
      return this.getTemplateSets().named(RESOURCE_TEMPLATE_SET_NAME, ResourceTemplateSet.class);
    } else {
      return this.getTemplateSets().register(RESOURCE_TEMPLATE_SET_NAME, ResourceTemplateSet.class);
    }
  }

  /**
   * Register a single primary template set for resource templates.
   *
   * @param configureAction the action to perform on the primary resource template set
   * @since 2.0.0
   */
  default void resources(final @NotNull Action<? super ResourceTemplateSet> configureAction) {
    this.resources().configure(Objects.requireNonNull(configureAction, "configureAction"));
  }

  // batches, at src/<set>/templates/<batch>

  /**
   * Get all currently registered template sets for this source set.
   *
   * @return the template sets
   * @since 2.0.0
   */
  @NotNull PolymorphicDomainObjectContainer<TemplateSet> getTemplateSets();

  /**
   * Configure template sets that apply to this source set.
   *
   * @param configurer the action to perform
   * @since 2.0.0
   */
  default void templateSets(final @NotNull Action<PolymorphicDomainObjectContainer<TemplateSet>> configurer) {
    Configurable.configure(this.getTemplateSets(), configurer);
  }
}
