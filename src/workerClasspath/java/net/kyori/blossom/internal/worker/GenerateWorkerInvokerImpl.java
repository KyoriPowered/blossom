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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.gradle.api.GradleException;
import org.gradle.api.InvalidUserDataException;
import org.jetbrains.annotations.Nullable;

public class GenerateWorkerInvokerImpl implements GenerateWorkerInvoker {
  private static final String FILE_NAME_CACHE_DISAMBIGUATOR = "###";
  private static final String PEBBLE_EXTENSION = ".peb";

  @Override
  public void generate(
    final TemplateParams globalParams,
    final Set<TemplateParams> variantParams,
    final Set<Path> includePaths,
    final Set<Path> sourcePaths,
    final Path outputDirectory,
    final @Nullable String header
  ) throws IOException {
    // By default, resolves FS paths
    // todo: restrict inputs to inputs and includes
    final Loader<?> loader = this.makeLoader(sourcePaths, includePaths);
    final PebbleEngine engine = new PebbleEngine.Builder()
      .autoEscaping(false) // no html escaping
      .defaultLocale(Locale.ROOT)
      .loader(loader)
      // .cacheActive(false) // xX: overlap between file names and template names causes issues
      .strictVariables(true) // make sure to fail when vars are not present
      .build();

    final Set<Map<String, Object>> variants = PropertyFileIO.prepareDataForGeneration(globalParams, variantParams);

    final Set<String> availableTemplates = this.collectTemplateNames(sourcePaths);
    final Set<String> seenOutputs = new HashSet<>();

    for (final String relativePath : availableTemplates) {
      // Parse the template
      final PebbleTemplate fileNameTemplate = engine.getLiteralTemplate(FILE_NAME_CACHE_DISAMBIGUATOR + relativePath);
      final PebbleTemplate template = engine.getTemplate(relativePath);

      // Then generate outputs for every variant
      for (final Map<String, Object> variant : variants) {
        String outputFile = this.evaluateToString(fileNameTemplate, variant)
          .substring(FILE_NAME_CACHE_DISAMBIGUATOR.length());
        if (outputFile.endsWith(PEBBLE_EXTENSION)) {
          outputFile = outputFile.substring(0, outputFile.length() - PEBBLE_EXTENSION.length());
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
            writer.newLine();
          }
          template.evaluate(writer, variant);
        }
      }
    }

  }

  private Loader<?> makeLoader(final Set<Path> sourcePaths, final Set<Path> includePaths) {
    final List<Loader<?>> loaders = new ArrayList<>();
    for (final Path sourceDir : sourcePaths) {
      final Loader<?> sourceLoader = new FileLoader();
      sourceLoader.setPrefix(sourceDir.toAbsolutePath().toString());
      loaders.add(sourceLoader);
    }

    if (!includePaths.isEmpty()) {
      for (final Path includesDir : includePaths) {
        final Loader<?> includesLoader = new FileLoader();
        includesLoader.setPrefix(includesDir.toAbsolutePath().toString());
      }
    }

    switch (loaders.size()) {
      case 0: throw new GradleException("No sources directories declared!");
      case 1: return loaders.get(0);
      default: return new DelegatingLoader(loaders);
    }
  }

  private Set<String> collectTemplateNames(final Set<Path> sourceDirs) {
    final Set<String> templateNames = new HashSet<>();
    for (final Path sourcePath : sourceDirs) {
      try {
        Files.walkFileTree(sourcePath, new FileVisitor<>() {
          // @formatter:off
          @Override public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) { return FileVisitResult.CONTINUE; }
          @Override public FileVisitResult visitFileFailed(final Path file, final IOException exc) { return FileVisitResult.CONTINUE; }
          @Override public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) { return FileVisitResult.CONTINUE; }
          // @formatter:on

          @Override
          public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) {
            // Parse the template
            templateNames.add(sourcePath.relativize(file).toString());
            return FileVisitResult.CONTINUE;
          }
        });
      } catch (final IOException ex) {
        throw new GradleException("Exception encountered when gathering template names in source directory '" + sourcePath + "'", ex);
      }
    }

    return Set.copyOf(templateNames);
  }

  private String evaluateToString(final PebbleTemplate template, final Map<String, Object> data) throws IOException {
    final StringWriter writer = new StringWriter();
    template.evaluate(writer, data);
    return writer.toString();
  }
}
