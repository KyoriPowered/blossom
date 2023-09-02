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

import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.loader.DelegatingLoader;
import io.pebbletemplates.pebble.loader.FileLoader;
import io.pebbletemplates.pebble.loader.Loader;
import io.pebbletemplates.pebble.template.PebbleTemplate;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.kyori.blossom.internal.TemplateSetInternal;
import net.kyori.mammoth.Properties;
import org.gradle.api.DefaultTask;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Generate real files based on templates and input parameters.
 *
 * @since 2.0.0
 */
public abstract class GenerateTemplates extends DefaultTask {
  private static final String FILE_NAME_CACHE_DISAMBIGUATOR = "###";
  private static final String PEBBLE_EXTENSION = ".peb";

  /**
   * The set the template is generated from.
   *
   * @return the template set
   * @since 2.0.0
   */
  @Nested
  public abstract @NotNull Property<TemplateSet> getBaseSet();

  /**
   * Files that can be included in templates, but that are not themselves templates.
   *
   * @return the files to include
   * @since 2.0.0
   */
  @InputDirectory
  @Optional
  public abstract @NotNull DirectoryProperty getIncludesDirectory(); // for files that can be included but are not themselves evaluated

  /**
   * Source directory for templates to process.
   *
   * @return the source directory
   * @since 2.0.0
   */
  @InputDirectory
  public abstract @NotNull DirectoryProperty getSourceDirectory();

  /**
   * Destination directory for template output.
   *
   * @return the destination directory
   * @since 2.0.0
   */
  @OutputDirectory
  public abstract @NotNull DirectoryProperty getOutputDir();

  @TaskAction
  void generate() throws IOException {
    final Path sourceDirectory = this.getSourceDirectory().get().getAsFile().toPath();
    final Loader<?> sourceLoader = new FileLoader();
    sourceLoader.setPrefix(sourceDirectory.toAbsolutePath().toString());
    final Loader<?> loader;

    if (this.getIncludesDirectory().isPresent()) {
      final Loader<?> includesLoader = new FileLoader();
      includesLoader.setPrefix(this.getIncludesDirectory().get().getAsFile().getAbsolutePath());
      loader = new DelegatingLoader(Arrays.asList(sourceLoader, includesLoader));
    } else {
      loader = sourceLoader;
    }

    // By default, resolves FS paths
    // todo: restrict inputs to inputs and includes
    final PebbleEngine engine = new PebbleEngine.Builder()
      .autoEscaping(false) // no html escaping
      .defaultLocale(Locale.ROOT)
      .loader(loader)
      // .cacheActive(false) // xX: overlap between file names and template names causes issues
      .strictVariables(true) // make sure to fail when vars are not present
      .build();

    final Path outputDirectory = this.getOutputDir().get().getAsFile().toPath();
    Files.createDirectories(outputDirectory);
    final Set<Map<String, Object>> variants = ((TemplateSetInternal) this.getBaseSet().get()).prepareDataForGeneration();
    final @Nullable String header = Properties.finalized(this.getBaseSet().get().getHeader()).getOrNull();

    final Set<String> seenOutputs = new HashSet<>();
    Files.walkFileTree(sourceDirectory, new FileVisitor<>() {
      // @formatter:off
      @Override public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) { return FileVisitResult.CONTINUE; }
      @Override public FileVisitResult visitFileFailed(final Path file, final IOException exc) { return FileVisitResult.CONTINUE; }
      @Override public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) { return FileVisitResult.CONTINUE; }
      // @formatter:on

      @Override
      public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
        // Parse the template
        final String relativePath = sourceDirectory.relativize(file).toString();
        final PebbleTemplate fileNameTemplate = engine.getLiteralTemplate(GenerateTemplates.FILE_NAME_CACHE_DISAMBIGUATOR + relativePath);
        final PebbleTemplate template = engine.getTemplate(relativePath);

        // Then generate outputs for every variant
        for (final Map<String, Object> variant : variants) {
          String outputFile = GenerateTemplates.this.evaluateToString(fileNameTemplate, variant)
            .substring(GenerateTemplates.FILE_NAME_CACHE_DISAMBIGUATOR.length());
          if (outputFile.endsWith(GenerateTemplates.PEBBLE_EXTENSION)) {
            outputFile = outputFile.substring(0, outputFile.length() - GenerateTemplates.PEBBLE_EXTENSION.length());
          }

          if (!seenOutputs.add(outputFile)) {
            throw new InvalidUserDataException("Output file " + outputFile + " (a variant of input " + relativePath + ") has already been "
              + "written in another variant!");
          }

          final Path output = outputDirectory.resolve(outputFile);
          Files.createDirectories(output.getParent());
          try (final BufferedWriter writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {
            if (header != null) {
              writer.write(header);
              writer.write(System.lineSeparator());
            }
            template.evaluate(writer, variant);
          }
        }
        return FileVisitResult.CONTINUE;
      }
    });
  }

  private String evaluateToString(final PebbleTemplate template, final Map<String, Object> data) throws IOException {
    final StringWriter writer = new StringWriter();
    template.evaluate(writer, data);
    return writer.toString();
  }
}
