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
import net.kyori.blossom.GenerateTemplates;
import net.kyori.blossom.ResourceTemplateSet;
import org.gradle.api.file.Directory;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;

/**
 * A template set that is configured to attach its generated output as a resource directory.
 */
public abstract class ResourceTemplateSetImpl extends TemplateSetImpl implements ResourceTemplateSet {
  @Inject
  public ResourceTemplateSetImpl(final String name) {
    super(name);
  }

  @Override
  public Directory resolveOutputRoot(final Directory generatedDir) {
    return generatedDir.dir("resources");
  }

  @Override
  public void registerOutputWithSet(final SourceSet destination, final TaskProvider<GenerateTemplates> generateTask) {
    destination.getResources().srcDir(generateTask.map(GenerateTemplates::getOutputDir));
  }
}
