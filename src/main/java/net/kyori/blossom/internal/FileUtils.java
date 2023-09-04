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

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.jetbrains.annotations.NotNull;

public final class FileUtils {
  private static final Logger LOGGER = Logging.getLogger(FileUtils.class);

  private FileUtils() {
  }

  public static void createDirectoriesSymlinkSafe(final @NotNull Path directory) throws IOException {
    if (!Files.isDirectory(directory)) { // not checked properly by Files.createDirectories
      Files.createDirectories(directory);
    }
  }

  public static void deleteContents(final @NotNull Path directory) throws IOException {
    if (!Files.isDirectory(directory)) return;

    Files.walkFileTree(directory, new FileVisitor<>() {
      // @formatter:off
      @Override
      public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) { return FileVisitResult.CONTINUE; }
      // @formatter:on

      @Override
      public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
        Files.delete(file);
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFileFailed(final Path file, final IOException ex) {
        LOGGER.error("Failed to delete file {} while walking directory {}", file, directory, ex);
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
        if (!dir.equals(directory)) {
          Files.delete(dir);
        }
        return FileVisitResult.CONTINUE;
      }
    });
  }
}
