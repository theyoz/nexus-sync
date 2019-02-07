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

/**
 * @author Yoram Halberstam (yoram dot halberstam at gmail dot com)
 * @since 05/02/19
 */
public class Asset {
    private String downloadUrl;
    private String path;
    private String id;
    private String repository;
    private String format;
    private Checksum checksum;

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public Asset downloadUrl(final String downloadUrl) {
        this.downloadUrl = downloadUrl;

        return this;
    }

    public String getPath() {
        return path;
    }

    public Asset path(final String path) {
        this.path = path;

        return this;
    }

    public String getId() {
        return id;
    }

    public Asset id(final String id) {
        this.id = id;

        return this;
    }

    public String getRepository() {
        return repository;
    }

    public Asset repository(final String repository) {
        this.repository = repository;

        return this;
    }

    public String getFormat() {
        return format;
    }

    public Asset format(final String format) {
        this.format = format;

        return this;
    }

    public Checksum getChecksum() {
        return checksum;
    }

    public Asset checksum(final Checksum checksum) {
        this.checksum = checksum;

        return this;
    }
}
