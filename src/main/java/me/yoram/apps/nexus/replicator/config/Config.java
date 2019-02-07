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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.URL;
import java.util.Objects;

/**
 * @author Yoram Halberstam (yoram dot halberstam at gmail dot com)
 * @since 07/02/19
 */
public class Config extends Bean {
    private static final Logger LOG = LoggerFactory.getLogger(Config.class);
    private static Config instance = null;

    public static Config getInstance() {
        return instance;
    }

    public static void setInstance(final Config instance) {
        Config.instance = instance;
    }

    static URL createUrl(final String url) throws ConfigException {
        try {
            return new URL(url);
        } catch (Throwable t) {
            throw new ConfigException(String.format("Error parsing URL %s.", url), t);
        }
    }

    static void validateHost(final String host) throws ConfigException {
        try {
            InetAddress.getByName(host);
        } catch (Throwable t) {
            throw new ConfigException(String.format("Could not get IP for host %s.", host), t);
        }
    }

    public static Credentials createCredentials(String basicAuthString) throws ConfigException {
        if (basicAuthString == null) {
            return new Credentials();
        }

        int pos = basicAuthString.indexOf(':');

        if (pos == basicAuthString.length() - 1) {
            basicAuthString = basicAuthString.substring(0, pos);
            pos = -1;
        } else if (pos == 0) {
            throw new ConfigException(
                    "Credential starting wit semicolon, which means a user has not been given (format is user:password)");
        }

        if (pos == -1) {
            LOG.warn(LOG.isTraceEnabled() ?
                    String.format(
                            "Credentials for user %s given without password (format is user:password)",
                            basicAuthString) :
                    "Credentials given without password (format is user:password)");

            return new Credentials().user(basicAuthString);
        } else {
            return new Credentials()
                    .user(basicAuthString.substring(0, pos))
                    .password(basicAuthString.substring(pos + 1));
        }
    }

    private Node source;
    private Node dest;
    private boolean dryRun = false;

    public Config() {

    }

    public Node getSource() {
        return source;
    }

    public Config source(final Node source) {
        this.source = source;

        return this;
    }

    public Node getDest() {
        return dest;
    }

    public Config dest(final Node dest) {
        this.dest = dest;

        return this;
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public Config dryRun(final boolean dryRun) {
        this.dryRun = dryRun;

        return this;
    }

    @Override
    public void validate(String hint) throws ConfigException {
        super.validate(hint);
        source.validate("Validating Source Configuration.");
        dest.validate("Validating Destination Configuration.");

        String src = source.getUrlConfig().prefixUrl() + source.getUrlConfig().getRepository();
        String dst = dest.getUrlConfig().prefixUrl() + dest.getUrlConfig().getRepository();

        if (Objects.equals(src,dst)) {
            throw new ConfigException("Source and Destination are the same");
        }
    }
}
