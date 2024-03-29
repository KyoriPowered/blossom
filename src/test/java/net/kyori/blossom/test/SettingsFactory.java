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

import java.io.IOException;
import net.kyori.mammoth.test.TestContext;

public final class SettingsFactory {
  private SettingsFactory() {
  }

  public static void writeSettings(final TestContext ctx, final String projectName) throws IOException {
    ctx.writeText("settings.gradle",
      "rootProject.name = '" + projectName + "'\n" +
        "\n" +
        "dependencyResolutionManagement {\n" +
        "  repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS\n" +
        "  repositories {\n" +
        "    mavenCentral()\n" +
        "  }\n" +
        "}\n");
  }
}
