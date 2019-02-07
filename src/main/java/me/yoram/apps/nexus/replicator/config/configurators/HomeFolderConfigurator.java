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
package me.yoram.apps.nexus.replicator.config.configurators;

import me.yoram.apps.nexus.replicator.config.Config;
import me.yoram.apps.nexus.replicator.api.ConfiguratorApi;
import me.yoram.apps.nexus.replicator.config.Node;
import me.yoram.apps.nexus.replicator.config.UrlConfig;
import me.yoram.apps.nexus.replicator.exceptions.ConfigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Yoram Halberstam (yoram dot halberstam at gmail dot com)
 * @since 07/02/19
 */
public class HomeFolderConfigurator implements ConfiguratorApi {
    private static final Logger LOG = LoggerFactory.getLogger(HomeFolderConfigurator.class);

    private static File configFile;

    static {
        final String fileOverride = System.getProperty("replicator.config");

        if (fileOverride != null) {
            LOG.info(String.format(
                    "Home Folder configuration override (-Dreplicator.config=/path/configfile) using %s",
                    fileOverride));
            configFile = new File(fileOverride);
        } else {
            configFile = new File(new File(System.getProperty("user.home")), "/.nexus-sync/.config");
        }
    }

    static void setConfigFile(File configFile) {
        HomeFolderConfigurator.configFile = configFile;
    }

    @Override
    public Config getConfig() throws ConfigException {
        try {
            final Properties props = new Properties();

            try (final InputStream in = new FileInputStream(configFile)) {
                props.load(in);
            }

            return new Config()
                    .source(new Node()
                            .urlConfig(
                                    new UrlConfig()
                                            .url(props.getProperty("source.url"))
                                            .repository(props.getProperty("source.repository")))
                            .credentials(Config.createCredentials(props.getProperty("source.credentials"))))
                    .dest(new Node()
                            .urlConfig(
                                    new UrlConfig()
                                            .url(props.getProperty("dest.url"))
                                            .repository(props.getProperty("dest.repository")))
                            .credentials(Config.createCredentials(props.getProperty("dest.credentials"))));
        } catch (Throwable t) {
            throw new ConfigException(t.getMessage(), t);
        }
    }

    @Override
    public String getName() {
        return "Home Folder";
    }

    @Override
    public String getDescription() {
        return String.format("Configuration has been taken from %s", configFile.getAbsolutePath());
    }
}
