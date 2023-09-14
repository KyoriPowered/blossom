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

import io.pebbletemplates.pebble.error.LoaderException;
import io.pebbletemplates.pebble.loader.Loader;
import io.pebbletemplates.pebble.utils.PathUtils;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Nullable;

final class MultiDirectoryLoader implements Loader<String> {
  private final List<Path> directories;
  private final Charset charset;

  MultiDirectoryLoader(final List<Path> directories, final Charset charset) {
    this.directories = List.copyOf(directories);
    this.charset = charset;
  }

  @Override
  public Reader getReader(final String templateName) {
    final Path file = this.findFile(templateName);
    if (file != null) {
      try {
        return Files.newBufferedReader(file, this.charset);
      } catch (final IOException ex) {
        throw new LoaderException(ex, "Could not load template \"" + templateName + "\" from file \"" + file + "\"");
      }
    }
    throw new LoaderException(null, "Could not find template \"" + templateName + "\" in any of: " + this.directories.stream().map(Path::toString).collect(Collectors.joining("; ")));
  }

  private @Nullable Path findFile(final String templateName) {
    for (final Path path : this.directories) {
      final Path file = path.resolve(templateName);
      if (Files.isRegularFile(file)
        // guard against escaping the directory
        && path.relativize(file).toString().equals(templateName)) {
        return file;
      }
    }
    return null;
  }

  @Override
  public void setSuffix(final String suffix) {
    throw new UnsupportedOperationException("Not used by Blossom");
  }

  @Override
  public void setPrefix(final String prefix) {
    throw new UnsupportedOperationException("Not used by Blossom");
  }

  @Override
  public void setCharset(final String charset) {
    throw new UnsupportedOperationException("Not used by Blossom");
  }

  @Override
  public String resolveRelativePath(final String relativePath, final String anchorPath) {
    return PathUtils.resolveRelativePath(relativePath, anchorPath, File.separatorChar);
  }

  @Override
  public String createCacheKey(final String templateName) {
    return templateName;
  }

  @Override
  public boolean resourceExists(final String templateName) {
    final Path path = this.findFile(templateName);
    return path != null && Files.isRegularFile(path);
  }
}
