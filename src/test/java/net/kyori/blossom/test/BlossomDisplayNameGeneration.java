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
package net.kyori.blossom.test;

import java.lang.reflect.Method;
import org.junit.jupiter.api.DisplayNameGenerator;

public class BlossomDisplayNameGeneration extends DisplayNameGenerator.Standard {
  @Override
  public String generateDisplayNameForMethod(final Class<?> clazz, final Method testMethod) {
    final String name = testMethod.getName();
    if (name.startsWith("test") && name.length() > 5) {
      return Character.toLowerCase(name.charAt(4)) + name.substring(5);
    } else {
      return name;
    }
  }
}
