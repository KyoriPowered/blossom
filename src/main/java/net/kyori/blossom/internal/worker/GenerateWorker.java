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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.gradle.api.GradleException;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;

public abstract class GenerateWorker implements WorkAction<GenerateWorker.Params> {
  public interface Params extends WorkParameters {
    // parameters + data files
    Property<String> getSetName();

    MapProperty<String, Object> getGlobalParameters();

    ConfigurableFileCollection getGlobalParameterFiles();

    MapProperty<String, Map<String, Object>> getVariantParameters();

    MapProperty<String, FileCollection> getVariantFiles();

    Property<String> getHeader();

    ConfigurableFileCollection getSourceDirectories();

    ConfigurableFileCollection getIncludesDirectories();

    DirectoryProperty getDestinationDirectory();
  }

  @Inject
  public GenerateWorker() {
  }

  @Override
  public void execute() {
    final GenerateWorkerInvoker invoker;
    try {
      invoker = (GenerateWorkerInvoker) Class.forName("net.kyori.blossom.internal.worker.GenerateWorkerInvokerImpl").getConstructor().newInstance();
    } catch (final InstantiationException
                   | IllegalAccessException
                   | InvocationTargetException
                   | NoSuchMethodException
                   | ClassNotFoundException ex) {
      throw new GradleException("Failed to access Blossom worker stub: " + ex.getMessage(), ex);
    }

    final Params params = this.getParameters();
    final var globalParams = new TemplateParams(
      params.getSetName().get(),
      toPaths(params.getGlobalParameterFiles()),
      params.getGlobalParameters().getOrElse(Map.of())
    );
    final Set<TemplateParams> variantParams = this.toVariantParameters(params.getVariantFiles().get(), params.getVariantParameters().get());

    try {
      invoker.generate(
        globalParams,
        variantParams,
        toPaths(params.getIncludesDirectories()),
        toPaths(params.getSourceDirectories()),
        params.getDestinationDirectory().get().getAsFile().toPath(),
        params.getHeader().getOrNull()
      );
    } catch (final IOException ex) {
      throw new GradleException("Failed to process templates:" + ex.getMessage(), ex);
    }
  }

  private Set<TemplateParams> toVariantParameters(final Map<String, ? extends FileCollection> variantFiles, final Map<String, Map<String, Object>> variantProperties) {
    final Set<TemplateParams> ret = new HashSet<>(variantFiles.size());
    for (final String variantName : variantFiles.keySet()) {
      ret.add(new TemplateParams(
        variantName,
        toPaths(variantFiles.get(variantName)),
        variantProperties.getOrDefault(variantName, Map.of())
      ));
    }

    return ret;
  }

  private static Set<Path> toPaths(final FileCollection files) {
    return files.getFiles().stream().map(File::toPath).collect(Collectors.toUnmodifiableSet());
  }
}
