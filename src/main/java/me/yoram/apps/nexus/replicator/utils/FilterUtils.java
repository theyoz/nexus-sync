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
package me.yoram.apps.nexus.replicator.utils;

import me.yoram.apps.nexus.replicator.dto.Asset;
import me.yoram.apps.nexus.replicator.dto.Component;
import me.yoram.apps.nexus.replicator.dto.DecoratedAsset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Yoram Halberstam (yoram dot halberstam at gmail dot com)
 * @since 07/02/19
 */
public class FilterUtils {
    public static boolean in(final String value, final String... filter) {
        for (final String s: filter) {
            if ((s == null && value == null) || (s != null && s.equalsIgnoreCase(value))) {
                return true;
            }
        }

        return false;
    }

    public static Collection<DecoratedAsset> deployableAssets(final Collection<DecoratedAsset> source) {
        return source
                .parallelStream()
                .filter(asset -> !in(asset.getExtension(), "sha1", "md5", "asc"))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public static Collection<DecoratedAsset> toDecoratedAssetCollection(final Component cp, final Collection<Asset> assets) {
        return assets
                .parallelStream()
                .map(asset -> new DecoratedAsset(cp, asset))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public static Map<String, Component> toMapKeyIsOID(final Collection<Component> col) {
        return col.parallelStream()
                .collect(
                        Collectors.toMap(o -> "", o -> (Component)o));
    }
    public static Collection<Component> differs(
            final Collection<Component> sourceCol, final Collection<Component> destCol) {
        return sourceCol
                .parallelStream()
                .filter(sourceComp -> {
                    var destComp = destCol
                            .parallelStream()
                            .anyMatch(comp -> comp.getName().equals(sourceComp.getName()) &&
                                    comp.getGroup().equals(sourceComp.getGroup()) &&
                                    comp.getVersion().equals(sourceComp.getVersion()));

                    // TODO BETTER EQUALS FOR REDEPLOY
                    return false;
                }).collect(Collectors.toList());
    }

    private FilterUtils() {}
}
