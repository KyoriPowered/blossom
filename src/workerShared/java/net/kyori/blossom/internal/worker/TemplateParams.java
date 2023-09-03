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
package net.kyori.blossom.internal.worker;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

public final class TemplateParams {
  private final String name;
  private final Set<Path> files;
  private final Map<String, Object> data;

  public TemplateParams(final String name, final Set<Path> files, final Map<String, Object> data) {
    this.name = name;
    this.files = files;
    this.data = data;
  }

  public String name() {
    return this.name;
  }

  public Set<Path> files() {
    return this.files;
  }

  public Map<String, Object> data() {
    return this.data;
  }
}
