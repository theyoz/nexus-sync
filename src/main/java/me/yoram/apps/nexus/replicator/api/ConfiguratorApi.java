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

import me.yoram.apps.nexus.replicator.api.ApiProvider;
import me.yoram.apps.nexus.replicator.config.Config;
import me.yoram.apps.nexus.replicator.exceptions.ConfigException;

/**
 * @author Yoram Halberstam (yoram dot halberstam at gmail dot com)
 * @since 07/02/19
 */
public interface ConfiguratorApi extends ApiProvider {
    default void setCommandLine(String[] args) {}
    Config getConfig() throws ConfigException;
}
