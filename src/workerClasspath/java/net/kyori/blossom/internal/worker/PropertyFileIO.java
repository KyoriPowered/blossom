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

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.gradle.api.GradleException;
import org.gradle.api.InvalidUserDataException;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.exceptions.YamlEngineException;

final class PropertyFileIO {

  private PropertyFileIO() {
  }

  public static Set<Map<String, Object>> prepareDataForGeneration(
    final TemplateParams globalParams,
    final Set<TemplateParams> variantParams
  ) {
    final Map<String, Map<String, Object>> configData = loadConfig(globalParams.name(), globalParams.files(), !variantParams.isEmpty());
    if (variantParams.isEmpty()) {
      // non-variant mode
      final Map<String, Object> result = configData.get(null);
      if (result == null) {
        return Set.of(globalParams.data());
      } else {
        result.putAll(globalParams.data());
        return Collections.singleton(result);
      }
    } else {
      // figure out any global data
      Map<String, Object> global = configData.remove(null);
      if (global == null) {
        global = globalParams.data();
      } else {
        global.putAll(globalParams.data());
      }
      final Set<Map<String, Object>> output = new HashSet<>();
      // then get the per-variant bits
      for (final TemplateParams variant : variantParams) {
        // global, from file
        final Map<String, Object> variantData = new LinkedHashMap<>(global);

        // variant, from global files
        final Map<String, Object> variantFromGlobalFile = configData.remove(variant.name());
        if (variantFromGlobalFile != null) {
          variantData.putAll(variantFromGlobalFile);
        }

        // variant, from variant files
        final Map<String, Object> variantFromFile = loadConfig(globalParams.name(), variant.files(), false).get(null);
        if (variantFromFile != null) {
          variantData.putAll(variantFromFile);
        }

        // variant, in-memory
        variantData.putAll(variant.data());

        output.add(variantData);
      }

      if (!configData.isEmpty()) {
        throw new InvalidUserDataException("Unknown variants declared in file for template set " + globalParams.name() + ": " + configData.keySet());
      }

      return output;
    }
  }

  private static Map<String, Map<String, Object>> loadConfig(final String templateSetName, final Set<Path> files, final boolean useVariants) {
    final LoadSettings settings = LoadSettings.builder()
      .setLabel("Template set " + templateSetName)
      .build();
    final Load load = new Load(settings);

    final Map<String, Map<String, Object>> templateParams = new HashMap<>();
    for (final Path file : files) {
      if (!Files.isRegularFile(file)) {
        continue;
      }
      try (final BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
        final Object data = load.loadFromReader(reader);
        unmarshalData(templateParams, data, useVariants);
      } catch (final YamlEngineException ex) {
        throw new InvalidUserDataException("Invalid input in " + file, ex);
      } catch (final IOException ex) {
        throw new GradleException("Failed to load data from " + file, ex);
      }
    }
    return templateParams;
  }

  private static void unmarshalData(final Map<String, Map<String, Object>> output, final Object data, final boolean useVariants) {
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
          output.put(String.valueOf(variant.getKey()), makeStringKeys((Map<?, ?>) variant.getValue()));
        }
      }
    }

    output.put(null, makeStringKeys((Map<?, ?>) data));
  }

  private static Map<String, Object> makeStringKeys(final Map<?, ?> map) {
    final Map<String, Object> ret = new LinkedHashMap<>();
    for (final Map.Entry<?, ?> entry : map.entrySet()) {
      ret.put(entry.getKey() == null ? null : entry.getKey().toString(), entry.getValue());
    }
    return ret;
  }
}
