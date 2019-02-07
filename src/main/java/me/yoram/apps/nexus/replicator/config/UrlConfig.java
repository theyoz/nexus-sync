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
package me.yoram.apps.nexus.replicator.config;

import me.yoram.apps.nexus.replicator.api.Bean;
import me.yoram.apps.nexus.replicator.exceptions.ConfigException;

import java.net.URL;

/**
 * @author Yoram Halberstam (yoram dot halberstam at gmail dot com)
 * @since 07/02/19
 */
public class UrlConfig extends Bean {
    public enum Protocol {
        HTTP("http", 80),
        HTTPS("https", 443);

        private final String protocol;
        private final int port;

        Protocol(final String protocol, final int port) {
            this.protocol = protocol;
            this.port = port;
        }

        private static Protocol byProtocol(final String protocol) {
            for (final Protocol res: Protocol.values()) {
                if (res.protocol.equalsIgnoreCase(protocol)) {
                    return res;
                }
            }

            return null;
        }

    }

    private Protocol protocol;
    private String host;
    private int port = -1;
    private String repository;

    public UrlConfig url(final String url) throws ConfigException {
        if (url == null) {
            this.protocol = null;
            this.host = null;
            this.port = -1;
            this.repository = null;
        } else {
            final URL u = Config.createUrl(url);

            protocol = Protocol.byProtocol(u.getProtocol());

            if (protocol == null) {
                throw new ConfigException(
                        String.format("Protocol %s not handled, only http or https", u.getProtocol()));
            }

            host = u.getHost();

            this.port = u.getPort();
        }

        return this;
    }

    public String prefixUrl() {
        return protocol.protocol + "://" + host + (this.port == -1 ? "" : ":" + this.port);
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public UrlConfig protocol(final Protocol protocol) {
        this.protocol = protocol;

        return this;
    }

    public String getHost() {
        return host;
    }

    public UrlConfig host(final String host) {
        this.host = host;

        return this;
    }

    public int getPort() {
        return port;
    }

    public UrlConfig port(final int port) {
        this.port = port;

        return this;
    }

    public String getRepository() {
        return repository;
    }

    public UrlConfig repository(final String repository) {
        this.repository = repository;

        return this;
    }

    @Override
    public void validate(final String hint) throws ConfigException {
        super.validate(hint);

        Config.validateHost(host);

        if (repository == null) {
            throw new ConfigException(hint + " Repository not specified.");
        }

        if (protocol == null) {
            protocol = Protocol.HTTP;
        }
    }
}
