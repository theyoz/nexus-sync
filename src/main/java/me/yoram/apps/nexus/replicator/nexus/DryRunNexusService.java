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
package me.yoram.apps.nexus.replicator.nexus;

import me.yoram.apps.nexus.replicator.config.Node;
import me.yoram.apps.nexus.replicator.dto.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Yoram Halberstam (yoram dot halberstam at gmail dot com)
 * @since 07/02/19
 */
public class DryRunNexusService extends NexusService{
    private static final Logger LOG = LoggerFactory.getLogger(DryRunNexusService .class);

    @Override
    public void upload(Node source, Node dest, Component component) {
        LOG.info(
                String.format(
                        "Component %s:%s:%s on %s/{%s} will be uploaded to %s/{%s}.",
                        component.getGroup(),
                        component.getName(),
                        component.getVersion(),
                        source.getUrlConfig().prefixUrl(),
                        source.getUrlConfig().getRepository(),
                        dest.getUrlConfig().prefixUrl(),
                        dest.getUrlConfig().getRepository()));
    }
}
