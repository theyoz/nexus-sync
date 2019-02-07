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
package me.yoram.apps.nexus.replicator.dto;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Objects;

/**
 * @author Yoram Halberstam (yoram dot halberstam at gmail dot com)
 * @since 07/02/19
 */
public class TestDecoratedAsset {
    @DataProvider
    public Object[][] dptClassifierAndExtension() {
        return new Object[][] {
                {
                    new Component()
                            .name("name")
                            .version("123")
                            .addAsset(new Asset().downloadUrl("http://localhost/a/path/name-123.jar")),
                        null,
                        "jar"
                },
                {
                    new Component()
                            .name("name")
                            .version("123")
                            .addAsset(new Asset().downloadUrl("http://localhost/a/path/name-123-sources.jar")),
                        "sources",
                        "jar"
                },
                {
                    new Component()
                            .name("name")
                            .version("123")
                            .addAsset(new Asset().downloadUrl("http://localhost/a/path/name-123")),
                        null,
                        null
                },
                {
                    new Component()
                            .name("name")
                            .version("123")
                            .addAsset(new Asset().downloadUrl("http://localhost/a/path/name-123-sources")),
                        "sources",
                        null
                },
                {
                        new Component()
                                .name("name")
                                .version("123")
                                .addAsset(new Asset().downloadUrl("http://localhost/a/path/name-123.pom.jar")),
                        null,
                        "jar"
                },
        };
    }

    @Test(dataProvider = "dptClassifierAndExtension")
    public void testClassifierAndExtension(Component component, String expectedClassifier, String expectedExtension) {
        DecoratedAsset asset = new DecoratedAsset(component, component.getAssets().iterator().next());

        assert Objects.equals(asset.getClassifier(), expectedClassifier) : String.format(
                "Classifier expected was %s but %s returned", expectedClassifier, asset.getClassifier());

        assert Objects.equals(asset.getExtension(), expectedExtension) : String.format(
                "Extension expected was %s but %s returned", expectedExtension, asset.getExtension());
    }
}
