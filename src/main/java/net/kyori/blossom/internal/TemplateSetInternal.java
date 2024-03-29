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

import net.kyori.blossom.GenerateTemplates;
import net.kyori.blossom.TemplateSet;
import org.gradle.api.file.Directory;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;

/**
 * Internal hooks for template sets.
 */
public interface TemplateSetInternal extends TemplateSet {
  // resolve an output directory for templates generated from this set, given build/generated/ as a base
  Directory resolveOutputRoot(final Directory generatedDir);

  void registerOutputWithSet(final SourceSet destination, final TaskProvider<GenerateTemplates> generateTask);
}
