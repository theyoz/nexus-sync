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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collection;

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
}
