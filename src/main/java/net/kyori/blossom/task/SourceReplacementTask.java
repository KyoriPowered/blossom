/*
 * A Gradle plugin for the creation of Minecraft mods and MinecraftForge plugins.
 * Copyright (C) 2013 Minecraft Forge
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
package net.kyori.blossom.task;

import com.google.common.base.Charsets;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.io.Files;
import groovy.lang.Closure;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryTree;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.util.PatternSet;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class SourceReplacementTask extends DefaultTask {
  @Input private final Map<String, Object> tokenReplacementsGlobal = Maps.newHashMap();
  @Input private final List<String> tokenReplacementsGlobalLocations = Lists.newArrayList();
  @Input private final Multimap<String, Map<String, Object>> tokenReplacementsByFile = HashMultimap.create();
  @OutputDirectory File output;
  @InputFiles private SourceDirectorySet input;

  /**
   * Perform the source replacement task.
   *
   * @throws IOException
   */
  @TaskAction
  public void run() throws IOException {
    final PatternSet patternSet = new PatternSet();
    patternSet.setIncludes(this.input.getIncludes());
    patternSet.setExcludes(this.input.getExcludes());

    if(this.output.exists()) {
      // Remove the output directory if it exists to prevent any possible conflicts
      deleteDirectory(this.output);
    }

    this.output.mkdirs();
    this.output = this.output.getCanonicalFile();

    // Resolve global and by-file replacements
    final Map<String, String> globalReplacements = this.resolveReplacementsGlobal();
    final Multimap<String, Map<String, String>> fileReplacements = this.resolveReplacementsByFile();

    for(final DirectoryTree dirTree : this.input.getSrcDirTrees()) {
      File dir = dirTree.getDir();

      // handle non-existent source directories
      if(!dir.exists() || !dir.isDirectory()) {
        continue;
      } else {
        dir = dir.getCanonicalFile();
      }

      // this could be written as .matching(source), but it doesn't actually work
      // because later on gradle casts it directly to PatternSet and crashes
      final FileTree tree = this.getProject().fileTree(dir).matching(this.input.getFilter()).matching(patternSet);

      for(final File file : tree) {
        final File destination = getDestination(file, dir, this.output);
        destination.getParentFile().mkdirs();
        destination.createNewFile();

        boolean wasChanged = false;
        String text = Files.toString(file, Charsets.UTF_8);

        if(this.isIncluded(file)) {
          for(final Map.Entry<String, String> entry : globalReplacements.entrySet()) {
            text = text.replaceAll(entry.getKey(), entry.getValue());
          }

          wasChanged = true;
        }

        final String path = this.getFilePath(file);
        final Collection<Map<String, String>> collection = fileReplacements.get(path);
        if(collection != null && !collection.isEmpty()) {
          for(final Map<String, String> map : collection) {
            for(final Map.Entry<String, String> entry : map.entrySet()) {
              text = text.replaceAll(entry.getKey(), entry.getValue());
            }
          }

          wasChanged = true;
        }

        if(wasChanged) {
          Files.write(text, destination, Charsets.UTF_8);
        } else {
          Files.copy(file, destination);
        }
      }
    }
  }

  /**
   * Get the file path relative to the project root.
   *
   * @param file The file
   * @return The file path
   */
  private String getFilePath(final File file) {
    final String path = file.getPath().replace(this.getProject().getProjectDir().getPath(), "").replace('\\', '/');
    if(path.charAt(0) == '/') {
      return path.substring(1);
    }

    return path;
  }

  /**
   * Determine if a file is included in global replacements.
   *
   * @param file The file
   * @return <code>true</code> if the file is included in global replacements, otherwise <code>false</code>
   * @throws IOException
   */
  private boolean isIncluded(final File file) throws IOException {
    if(this.tokenReplacementsGlobalLocations.isEmpty()) {
      // If there have been no global replacement locations set, but we do have global replacements,
      // perform replacements in all files.
      return true;
    }

    final String path = file.getCanonicalPath().replace('\\', '/');
    for(final String include : this.tokenReplacementsGlobalLocations) {
      if(path.endsWith(include.replace('\\', '/'))) {
        return true;
      }
    }

    return false;
  }

  /**
   * Resolve global replacements.
   *
   * @return The global replacements
   */
  private Map<String, String> resolveReplacementsGlobal() {
    final Map<String, String> result = Maps.newHashMapWithExpectedSize(this.tokenReplacementsGlobal.size());

    for(final Map.Entry<String, Object> entry : this.tokenReplacementsGlobal.entrySet()) {
      if(entry.getKey() == null || entry.getValue() == null) {
        continue; // we don't deal with nulls.
      }

      Object value = entry.getValue();
      while(value instanceof Closure) {
        value = ((Closure<Object>) value).call();
      }

      result.put(Pattern.quote(entry.getKey()), value.toString());
    }

    return result;
  }

  /**
   * Resolve by-file replacements.
   *
   * @return The by-file replacements
   */
  private Multimap<String, Map<String, String>> resolveReplacementsByFile() {
    final Multimap<String, Map<String, String>> result = HashMultimap.create();

    for(final Map.Entry<String, Collection<Map<String, Object>>> maps : this.tokenReplacementsByFile.asMap().entrySet()) {
      for(final Map<String, Object> map : maps.getValue()) {
        for(final Map.Entry<String, Object> entry : map.entrySet()) {
          if(entry.getKey() == null || entry.getValue() == null) {
            continue;
          }

          Object value = entry.getValue();
          while(value instanceof Closure) {
            value = ((Closure<Object>) value).call();
          }

          result.put(maps.getKey(), ImmutableMap.of(Pattern.quote(entry.getKey()), value.toString()));
        }
      }
    }

    return result;
  }

  /**
   * Set the input location.
   *
   * @param input The input location
   */
  public void setInput(final SourceDirectorySet input) {
    this.input = input;
  }

  /**
   * Set the output location.
   *
   * @param output The output location
   */
  public void setOutput(final File output) {
    this.output = output;
  }

  /**
   * Set the global token replacements.
   *
   * @param map The global token replacements
   */
  public void setTokenReplacementsGlobal(final Map<String, Object> map) {
    this.tokenReplacementsGlobal.putAll(map);
  }

  /**
   * Set the global token replacement locations.
   *
   * @param locations The global token replacement locations
   */
  public void setTokenReplacementsGlobalLocations(final List<String> locations) {
    this.tokenReplacementsGlobalLocations.addAll(locations);
  }

  /**
   * Set the by-file token replacements.
   *
   * @param map The by-file token replacements
   */
  public void setTokenReplacementsByFile(final Multimap<String, Map<String, Object>> map) {
    this.tokenReplacementsByFile.putAll(map);
  }

  /**
   * Get the destination for a file.
   *
   * @param in The file
   * @param base The base path
   * @param baseOut The base output directory
   * @return The destination file
   * @throws IOException
   */
  private static File getDestination(final File in, final File base, final File baseOut) throws IOException {
    return new File(baseOut, in.getCanonicalPath().replace(base.getCanonicalPath(), ""));
  }

  /**
   * Delete a directory and its contents.
   *
   * @param directory The directory
   * @return The delete result
   */
  private static boolean deleteDirectory(final File directory) {
    if(directory.exists()) {
      final File[] files = directory.listFiles();
      if(files != null) {
        for(final File file : files) {
          if(file.isDirectory()) {
            deleteDirectory(file);
          } else {
            file.delete();
          }
        }
      }
    }

    return directory.delete();
  }
}
