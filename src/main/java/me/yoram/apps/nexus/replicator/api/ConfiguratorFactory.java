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
package me.yoram.apps.nexus.replicator.api;

import me.yoram.apps.nexus.replicator.config.configurators.HomeFolderConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author Yoram Halberstam (yoram dot halberstam at gmail dot com)
 * @since 07/02/19
 */
public class ConfiguratorFactory {
    private static final Logger LOG = LoggerFactory.getLogger(ConfiguratorFactory.class);

    private static final Collection<Class<? extends ConfiguratorApi>> PROVIDERS = Arrays.asList(
            HomeFolderConfigurator.class);

    public static Collection<ConfiguratorApi> getProviders(final String[] commandLine) {
        final Collection<ConfiguratorApi> res = new ArrayList<>();

        for (final Class<? extends ConfiguratorApi> clazz: PROVIDERS) {
            try {
                final Constructor<? extends ConfiguratorApi> c = clazz.getConstructor();
                final ConfiguratorApi api = c.newInstance();
                api.setCommandLine(commandLine);
                res.add(api);
            } catch (Throwable t) {
                LOG.warn(
                        String.format(
                                "Couldn't create instance of %s configurator. %s",
                                clazz.getSimpleName(), t.getMessage()), t);
            }
        }

        return res;
    }

    private ConfiguratorFactory() {}
}
