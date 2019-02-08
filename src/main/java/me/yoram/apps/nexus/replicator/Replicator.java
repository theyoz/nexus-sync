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
import me.yoram.apps.nexus.replicator.exceptions.ReplicatorException;
import me.yoram.apps.nexus.replicator.nexus.DryRunNexusService;
import me.yoram.apps.nexus.replicator.nexus.NexusService;
import me.yoram.apps.nexus.replicator.config.Config;
import me.yoram.apps.nexus.replicator.api.ConfiguratorApi;
import me.yoram.apps.nexus.replicator.api.ConfiguratorFactory;
import me.yoram.apps.nexus.replicator.dto.Asset;
import me.yoram.apps.nexus.replicator.dto.Component;
import me.yoram.apps.nexus.replicator.exceptions.ConfigException;
import me.yoram.apps.nexus.replicator.utils.FilterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

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
        var sourceComponents = api.getAllComponents(config.getSource());
        var destComponents = api.getAllComponents(config.getDest());

        final var sourceMap = FilterUtils.toMapCoordsAndComponents(sourceComponents.getItems());
        final var destMap = FilterUtils.toMapCoordsAndComponents(destComponents.getItems());
        final var missing = sourceMap.entrySet().stream()
                .filter(entry -> !destMap.containsKey(entry.getKey()))
                .collect(Collectors.toMap(o -> o.getKey(), o -> o.getValue()));

        if (missing.size() == 0) {
            LOG.info(String.format("Destination %s is up to date", config.getDest().getUrlConfig().prefixUrl()));
        } else {
            final var ints = new int[] {0, 0};
            missing.values().parallelStream().forEach(component -> {
                ints[0] = ints[0] + 1;

                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("Uploading component %d/%d", ints[0], sourceComponents.getItems().size()));
                }

                try {
                    api.upload(config.getSource(), config.getDest(), component);
                } catch (Throwable t) {
                    LOG.error(t.getMessage(), t);
                    ints[1] = ints[1] + 1;
                }
            });

            if (ints[1] != 0) {
                throw new ReplicatorException(String.format("There has been %d upload errors", ints[1]));
            }
        }
    }
}
