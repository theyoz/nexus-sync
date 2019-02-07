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
package me.yoram.apps.nexus.replicator;

import me.yoram.apps.nexus.replicator.api.NexusRepositoryApi;
import me.yoram.apps.nexus.replicator.api.NexusRepositoryFactory;
import me.yoram.apps.nexus.replicator.nexus.DryRunNexusService;
import me.yoram.apps.nexus.replicator.nexus.NexusService;
import me.yoram.apps.nexus.replicator.config.Config;
import me.yoram.apps.nexus.replicator.api.ConfiguratorApi;
import me.yoram.apps.nexus.replicator.api.ConfiguratorFactory;
import me.yoram.apps.nexus.replicator.dto.Asset;
import me.yoram.apps.nexus.replicator.dto.Component;
import me.yoram.apps.nexus.replicator.exceptions.ConfigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * @author Yoram Halberstam (yoram dot halberstam at gmail dot com)
 * @since 05/02/19
 */
public class Replicator {
    private static final Logger LOG = LoggerFactory.getLogger(Replicator.class);

    static Config loadConfig(String[] commandLine) throws ConfigException {
        for (final ConfiguratorApi api: ConfiguratorFactory.getProviders(commandLine)) {
            try {
                final Config config = api.getConfig();

                if (config != null) {
                    LOG.info(String.format("Trying to get config from %s...OK", api.getName()));
                    LOG.info(api.getDescription());

                    return config;
                } else {
                    LOG.info(String.format("Trying to get config from %s...FAILED", api.getName()));
                }
            } catch (Throwable t) {
                LOG.error(String.format("Trying to get config from %s...ERROR", api.getName()), t);
            }
        }

        throw new ConfigException("No Configuration found. The Configuration should be specified as follow (in order)");

    }

    public static void main(String[] args) throws Exception {
        final Config config = loadConfig(args);
        config.validate("Validating Config");
        LOG.info(
                String.format(
                        "Replicating from %s/{%s} to %s/{%s}",
                        config.getSource().getUrlConfig().prefixUrl(),
                        config.getSource().getUrlConfig().getRepository(),
                        config.getDest().getUrlConfig().prefixUrl(),
                        config.getDest().getUrlConfig().getRepository()));

        if (config.getSource().getCredentials().getBasicAuthenticationDigest() != null) {
            LOG.info(
                    String.format(
                            "Using user %s to authenticate with %s",
                            config.getSource().getCredentials().getUser(),
                            config.getSource().getUrlConfig().prefixUrl()));
        }

        if (config.getDest().getCredentials().getBasicAuthenticationDigest() != null) {
            LOG.info(
                    String.format(
                            "Using user %s to authenticate with %s",
                            config.getDest().getCredentials().getUser(),
                            config.getDest().getUrlConfig().prefixUrl()));
        }

        if (config.isDryRun()) {
            LOG.info("DRY RUN!!!!!!!");
            NexusRepositoryFactory.setInstance(new DryRunNexusService());
        } else {
            NexusRepositoryFactory.setInstance(new NexusService());

        }

        Config.setInstance(config);

        /*var res = NexusService.getInstance().getAllComponents(
                user, pass, "https://nexus.open-esb.net/service/rest/v1/components?repository=maven-releases");*/

        NexusRepositoryApi api = NexusRepositoryFactory.getInstance();
        var res = api.getAllComponents(config.getSource());

        for (Component c: res.getItems()) {
            api.upload(config.getSource(), config.getDest(), c);
        }

        //NexusService.getInstance().upload(user2, pass2, "https://nexus.backup.yoram.me/service/rest/v1/components?repository=maven-releases", cp, asset);
        //NexusService.getInstance().upload(user2, pass2, "http://localhost:8089/service/rest/v1/components?repository=maven-releases", cp, asset);
        //NexusService.getInstance().upload(user2, pass2, "http://localhost:8081/service/rest/v1/components?repository=maven-releases", cp, asset);
        //api.upload(config.getSource(), config.getDest(), cp);
        System.out.println(res);
        System.out.println(new Date().toString());
    }
}
