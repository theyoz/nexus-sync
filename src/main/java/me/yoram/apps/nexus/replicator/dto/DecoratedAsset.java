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
public class DecoratedAsset extends Asset {
    private final Asset instance;
    private final String classifier;
    private final String extension;

    public DecoratedAsset(final Component parent, final Asset instance) {
        super();

        this.instance = instance;

        final String url = instance.getDownloadUrl();
        int pos = url.lastIndexOf('.');
        this.extension = pos == -1 || pos == url.length() - 1 ? null : url.substring(pos + 1);

        pos = url.lastIndexOf("/");
        final String filepart =  url.substring(pos + 1);
        final int expectedLen = parent.getName().length() +
                parent.getVersion().length() +
                (this.extension == null ? 0 : this.extension.length()) +
                2;

        if (filepart.length() > expectedLen) {
            String s = filepart.substring(parent.getName().length() + parent.getVersion().length() + 1);

            if (s.startsWith("-")) {
                s = s.substring(1);

                if (this.extension != null) {
                    s =  s.substring(0, s.length() - (this.extension.length() + 1));
                }

                this.classifier = s;
            } else {
                this.classifier = null;
            }
        } else {
            this.classifier = null;
        }
    }

    @Override
    public String getDownloadUrl() {
        return instance.getDownloadUrl();
    }

    @Override
    public Asset downloadUrl(final String downloadUrl) {
        return instance.downloadUrl(downloadUrl);
    }

    @Override
    public String getPath() {
        return instance.getPath();
    }

    @Override
    public Asset path(final String path) {
        return instance.path(path);
    }

    @Override
    public String getId() {
        return instance.getId();
    }

    @Override
    public Asset id(final String id) {
        return instance.id(id);
    }

    @Override
    public String getRepository() {
        return instance.getRepository();
    }

    @Override
    public Asset repository(final String repository) {
        return instance.repository(repository);
    }

    @Override
    public String getFormat() {
        return instance.getFormat();
    }

    @Override
    public Asset format(final String format) {
        return instance.format(format);
    }

    @Override
    public Checksum getChecksum() {
        return instance.getChecksum();
    }

    @Override
    public Asset checksum(final Checksum checksum) {
        return instance.checksum(checksum);
    }

    public String getClassifier() {
        return classifier;
    }

    public String getExtension() {
        return extension;
    }
}
