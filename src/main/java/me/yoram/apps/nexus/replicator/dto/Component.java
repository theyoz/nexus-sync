/*
 * Copyright 2018 Yoram Halberstam (yoram dot halberstam at gmail dot com)
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.yoram.apps.nexus.replicator.dto;

import me.yoram.apps.nexus.replicator.api.Bean;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Yoram Halberstam (yoram dot halberstam at gmail dot com)
 * @since 05/02/19
 */
public class Component extends Bean {
    private String id;
    private String repository;
    private String format;
    private String group;
    private String name;
    private String version;
    private Collection<Asset> assets;

    public String getId() {
        return id;
    }

    public Component id(final String id) {
        this.id = id;

        return this;
    }

    public String getRepository() {
        return repository;
    }

    public Component repository(final String repository) {
        this.repository = repository;

        return this;
    }

    public String getFormat() {
        return format;
    }

    public Component format(final String format) {
        this.format = format;

        return this;
    }

    public String getGroup() {
        return group;
    }

    public Component group(final String group) {
        this.group = group;

        return this;
    }

    public String getName() {
        return name;
    }

    public Component name(final String name) {
        this.name = name;

        return this;
    }

    public String getVersion() {
        return version;
    }

    public Component version(final String version) {
        this.version = version;

        return this;
    }

    public Collection<Asset> getAssets() {
        return assets;
    }

    public Component assets(final Collection<Asset> assets) {
        this.assets = assets;

        return this;
    }

    public Component addAsset(final Asset asset) {
        if (this.assets == null) {
            this.assets = new ArrayList<>();
        }

        this.assets.add(asset);

        return this;
    }
}
