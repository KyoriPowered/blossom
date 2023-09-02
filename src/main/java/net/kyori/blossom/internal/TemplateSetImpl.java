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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import net.kyori.blossom.Variant;
import org.gradle.api.GradleException;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.jetbrains.annotations.NotNull;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.exceptions.YamlEngineException;

/**
 * A directory of templates.
 *
 * <p>While it's perfectly possible to </p>
 */
public abstract class TemplateSetImpl implements TemplateSetInternal {
  // shared
  private final ConfigurableFileCollection dataFiles;
  private final MapProperty<String, Object> properties;
  private final NamedDomainObjectContainer<Variant> variants;
  private final Property<String> header;
  private final String name;

  @Inject
  public TemplateSetImpl(final String name) {
    this.name = name;
    this.dataFiles = this.getObjects().fileCollection();
    this.properties = this.getObjects().mapProperty(String.class, Object.class);
    this.variants = this.getObjects().domainObjectContainer(Variant.class);
    this.header = this.getObjects().property(String.class);
  }

  @Inject
  protected abstract ObjectFactory getObjects();

  @Override
  public @NotNull String getName() {
    return this.name;
  }

  // global properties

  @Override
  public @NotNull ConfigurableFileCollection getDataFiles() { // if there are variants, reads per-variant data from a list under `variants`
    return this.dataFiles;
  }

  @Override
  public @NotNull MapProperty<String, Object> getProperties() {
    return this.properties;
  }

  @Override
  public @NotNull Property<String> getHeader() {
    return this.header;
  }

  // variant

  @Override
  public @NotNull NamedDomainObjectContainer<Variant> getVariants() {
    return this.variants;
  }

  @Override
  public Set<Map<String, Object>> prepareDataForGeneration() {
    final Map<String, Map<String, Object>> configData = this.loadConfig(this.getDataFiles(), !this.getVariants().isEmpty());
    if (this.getVariants().isEmpty()) {
      // non-variant mode
      final Map<String, Object> result = configData.get(null);
      if (result == null) {
        return Collections.singleton(this.getProperties().get());
      } else {
        result.putAll(this.getProperties().get());
        return Collections.singleton(result);
      }
    } else {
      // figure out any global data
      Map<String, Object> global = configData.remove(null);
      if (global == null) {
        global = this.getProperties().getOrElse(Collections.emptyMap());
      } else {
        global.putAll(this.getProperties().getOrElse(Collections.emptyMap()));
      }
      final Set<Map<String, Object>> output = new HashSet<>();
      // then get the per-variant bits
      for (final Variant variant : this.getVariants()) {
        // global, from file
        final Map<String, Object> variantData = new LinkedHashMap<>(global);

        // variant, from global files
        final Map<String, Object> variantFromGlobalFile = configData.remove(variant.getName());
        if (variantFromGlobalFile != null) {
          variantData.putAll(variantFromGlobalFile);
        }

        // variant, from variant files
        final Map<String, Object> variantFromFile = this.loadConfig(variant.getDataFiles(), false).get(null);
        if (variantFromFile != null) {
          variantData.putAll(variantFromFile);
        }

        // variant, in-memory
        variantData.putAll(variant.getProperties().getOrElse(Collections.emptyMap()));

        output.add(variantData);
      }

      if (!configData.isEmpty()) {
        throw new InvalidUserDataException("Unknown variants declared in file for template set " + this.getName() + ": " + configData.keySet());
      }

      return output;
    }
  }

  private Map<String, Map<String, Object>> loadConfig(final ConfigurableFileCollection files, final boolean useVariants) {
    final LoadSettings settings = LoadSettings.builder()
      .setLabel("Template set " + this.getName())
      .build();
    final Load load = new Load(settings);

    final Map<String, Map<String, Object>> templateParams = new HashMap<>();
    for (final File file : files.getAsFileTree()) {
      if (!file.isFile()) {
        continue;
      }
      try (final BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
        final Object data = load.loadFromReader(reader);
        this.unmarshalData(templateParams, data, useVariants);
      } catch (final YamlEngineException ex) {
        throw new InvalidUserDataException("Invalid input in " + file, ex);
      } catch (final IOException ex) {
        throw new GradleException("Failed to load data from " + file, ex);
      }
    }
    return templateParams;
  }

  private void unmarshalData(final Map<String, Map<String, Object>> output, final Object data, final boolean useVariants) {
    if (!(data instanceof Map<?, ?>)) {
      throw new InvalidUserDataException("Template data files must have a mapping as the root node");
    }

    final Map<?, ?> dataMap = (Map<?, ?>) data;
    if (useVariants) {
      final Object variants = dataMap.remove("variants");
      if (variants != null) {
        if (!(variants instanceof Map<?, ?>)) {
          throw new InvalidUserDataException("value of 'variants' entry must be a mapping of String to Map<String, Object>");
        }

        for (final Map.Entry<?, ?> variant : ((Map<?, ?>) variants).entrySet()) {
          if (!(variant.getValue() instanceof Map<?, ?>)) {
            throw new InvalidUserDataException("Variant '" + variant.getKey() + "' was expected to have a mapping value, but it was a " + variant.getValue().getClass());
          }
          output.put(String.valueOf(variant.getKey()), TemplateSetImpl.makeStringKeys((Map<?, ?>) variant.getValue()));
        }
      }
    }

    output.put(null, TemplateSetImpl.makeStringKeys((Map<?, ?>) data));
  }

  private static Map<String, Object> makeStringKeys(final Map<?, ?> map) {
    final Map<String, Object> ret = new LinkedHashMap<>();
    for (final Map.Entry<?, ?> entry : map.entrySet()) {
      ret.put(entry.getKey() == null ? null : entry.getKey().toString(), entry.getValue());
    }
    return ret;
  }
}
