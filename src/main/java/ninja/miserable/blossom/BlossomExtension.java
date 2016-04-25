/*
 * This file is part of Blossom, licensed under the GNU Lesser General Public License.
 * Copyright (C) 2015-2016 MiserableNinja
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
package ninja.miserable.blossom;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.gradle.api.Project;

import java.util.List;
import java.util.Map;

/**
 * The Blossom extension.
 */
public class BlossomExtension {

    /** The project this extension is for. */
    protected transient Project project;
    /** A collection of replacements to perform globally. */
    private final Map<String, Object> tokenReplacementsGlobal = Maps.newHashMap();
    /** A list of locations to perform global replacements. */
    private final List<String> tokenReplacementsGlobalLocations = Lists.newArrayList();
    /** A collection of replacements to perform for each file. */
    private final Multimap<String, Map<String, Object>> tokenReplacementsByFile = HashMultimap.create();

    public BlossomExtension(Blossom blossom) {
        this.project = blossom.project;
    }

    /**
     * Replace a token.
     *
     * @param token The token
     * @param replacement The replacement
     */
    public void replaceToken(Object token, Object replacement) {
        this.tokenReplacementsGlobal.put(token.toString(), replacement);
    }

    /**
     * @see {@link #replaceToken(Object, Object)}
     */
    public void replaceToken(Map<Object, Object> map) {
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            this.replaceToken(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Add a location where global tokens should be performed.
     *
     * @param location The location
     */
    public void replaceTokenIn(String location) {
        this.tokenReplacementsGlobalLocations.add(location);
    }

    /**
     * Replace a token in a specific file.
     *
     * @param token The token
     * @param replacement The replacement
     * @param file The file
     */
    public void replaceToken(Object token, Object replacement, Object file) {
        this.tokenReplacementsByFile.put(file.toString(), ImmutableMap.of(token.toString(), replacement));
    }

    /**
     * Gets the global token replacements.
     *
     * @return The global token replacements
     */
    public Map<String, Object> getTokenReplacementsGlobal() {
        return this.tokenReplacementsGlobal;
    }

    /**
     * Gets the global token replacement locations.
     *
     * @return The global token replacement locations
     */
    public List<String> getTokenReplacementsGlobalLocations() {
        return this.tokenReplacementsGlobalLocations;
    }

    /**
     * Gets the by-file token replacements.
     *
     * @return The by-file token replacements
     */
    public Multimap<String, Map<String, Object>> getTokenReplacementsByFile() {
        return this.tokenReplacementsByFile;
    }
}
