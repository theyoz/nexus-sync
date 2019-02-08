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

import com.google.gson.Gson;
import me.yoram.apps.nexus.replicator.api.NexusRepositoryFactory;
import me.yoram.apps.nexus.replicator.config.Config;
import me.yoram.apps.nexus.replicator.config.Credentials;
import me.yoram.apps.nexus.replicator.config.Node;
import me.yoram.apps.nexus.replicator.config.UrlConfig;
import me.yoram.apps.nexus.replicator.dto.Asset;
import me.yoram.apps.nexus.replicator.dto.Component;
import me.yoram.apps.nexus.replicator.dto.DecoratedAsset;
import me.yoram.apps.nexus.replicator.dto.ListComponentResponse;
import me.yoram.apps.nexus.replicator.nexus.NexusService;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * @author Yoram Halberstam (yoram dot halberstam at gmail dot com)
 * @since 07/02/19
 */
public class TestFilterUtils {
    @DataProvider
    public Object[][] dpIn() {
        return new Object[][] {
                {"abc", new String[] {"az", "ab"}, false},
                {"abc", new String[] {"az", "abc"}, true},
                {null, new String[] {"az", "abc"}, false},
                {null, new String[] {"az", "abc", null}, true}
        };
    }

    @Test(dataProvider = "dpIn")
    public void testIn(String value, String[] filter, boolean expectedResult) {
        assert FilterUtils.in(value, filter) == expectedResult : "failed";
    }

    @DataProvider
    public Object[][] dpDeployableAssets() {
        Component cp01 = new Component().name("artifact1").group("group1").version("0.1");

        return new Object[][] {
                {
                    Arrays.asList(
                            new DecoratedAsset(cp01, new Asset().downloadUrl("http://localhost/a/path/a-01.jar")),
                            new DecoratedAsset(cp01, new Asset().downloadUrl("http://localhost/a/path/a-01.jar.sha1")),
                            new DecoratedAsset(cp01, new Asset().downloadUrl("http://localhost/a/path/a-01.jar.md5")),
                            new DecoratedAsset(cp01, new Asset().downloadUrl("http://localhost/a/path/a-01-source.jar")),
                            new DecoratedAsset(cp01, new Asset().downloadUrl("http://localhost/a/path/a-01-source.jar.sha1")),
                            new DecoratedAsset(cp01, new Asset().downloadUrl("http://localhost/a/path/a-01-source.jar.md5")),
                            new DecoratedAsset(cp01, new Asset().downloadUrl("http://localhost/a/path/a-01.pom")),
                            new DecoratedAsset(cp01, new Asset().downloadUrl("http://localhost/a/path/a-01.pom.sha1")),
                            new DecoratedAsset(cp01, new Asset().downloadUrl("http://localhost/a/path/a-01.pom.md5"))),
                        Arrays.asList(
                                "http://localhost/a/path/a-01.jar",
                                "http://localhost/a/path/a-01-source.jar",
                                "http://localhost/a/path/a-01.pom")
                }
        };
    }

    @Test(dataProvider = "dpDeployableAssets")
    public void testDeployableAssets(Collection<DecoratedAsset> assets, Collection<String> expectedResult) {
        Collection<DecoratedAsset> res = FilterUtils.deployableAssets(assets);
        assert res.size() == expectedResult.size() : String.format("Collection\n%sExpected\n%s", res, expectedResult);

        for (DecoratedAsset asset: res) {
            assert expectedResult.contains(asset.getDownloadUrl()) :
                    String.format("Collection\n%sExpected to contain\n%s", res, asset.getDownloadUrl());
        }
    }

    @Test
    public void testGetCoords() {
        String res = FilterUtils.getCoords(new Component().group("aa").name("bb").version("11"));

        assert "aa:bb:11".equals(res) : String.format("Expected aa:bb:11 but %s returned", res);
    }

    @Test
    public void testToMapCoordsAndComponents() throws Exception {
        ListComponentResponse res;

        try (final InputStream in = TestFilterUtils.class.getResourceAsStream("/json/ListComponentResponse/01.json")) {
            res = new Gson().fromJson(new InputStreamReader(in), ListComponentResponse.class);
        }

        Map<String, Component> map = FilterUtils.toMapCoordsAndComponents(res.getItems());

        final Collection<String> expectedKeys = new ArrayList<>(
                Arrays.asList(
                        "net.open-esb.components:jmsbc:3.1.1",
                        "net.open-esb.components:xsdmodel:3.0.11",
                        "net.open-esb.core:test-components:3.1.2",
                        "net.open-esb.core:ri-clients:3.1.0",
                        "net.open-esb.components:filebc-installer:3.0.10",
                        "net.open-esb.core:jbi-ext:3.1.1",
                        "net.open-esb.components:restbc3-components-jbiadapter:3.0.11",
                        "net.open-esb.components:pojose-components-jbiadapter:3.1.1-beta",
                        "net.open-esb.components:common-util:3.1.1-beta",
                        "com.sun.encoder:hl7encoder-xsdextension:1.0"));

        for (Map.Entry<String, Component> entry: map.entrySet()) {
            String coords = FilterUtils.getCoords(entry.getValue());

            assert entry.getKey().equals(coords) : String.format(
                    "Entry coordinate (the key) should be %s but component inside is %s", entry.getKey(), coords);

            assert expectedKeys.contains(entry.getKey()) : String.format("Coordinqtes %s not expected", entry.getKey());

            expectedKeys.remove(entry.getKey());
        }

        assert expectedKeys.size() == 0 : String.format("Entries not found but expected: %s", expectedKeys);
    }

}
