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

import com.google.gson.Gson;
import me.yoram.apps.nexus.replicator.Replicator;
import me.yoram.apps.nexus.replicator.api.NexusRepositoryApi;
import me.yoram.apps.nexus.replicator.config.Config;
import me.yoram.apps.nexus.replicator.config.Node;
import me.yoram.apps.nexus.replicator.dto.Asset;
import me.yoram.apps.nexus.replicator.dto.Component;
import me.yoram.apps.nexus.replicator.dto.DecoratedAsset;
import me.yoram.apps.nexus.replicator.dto.ListComponentResponse;
import me.yoram.apps.nexus.replicator.exceptions.HttpException;
import me.yoram.apps.nexus.replicator.exceptions.ReplicatorException;
import me.yoram.apps.nexus.replicator.utils.FilterUtils;
import me.yoram.apps.nexus.replicator.utils.HttpUtils;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * @author Yoram Halberstam (yoram dot halberstam at gmail dot com)
 * @since 05/02/19
 */
public class NexusService implements NexusRepositoryApi {
    private static final Logger LOG = LoggerFactory.getLogger(NexusService.class);

    //static Collection<Asset>

    private final HttpClient client = HttpClient
            .newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .proxy(ProxySelector.getDefault())
            .build();

    public ListComponentResponse getAllComponents(Node node) throws ReplicatorException {
        try {
            final var baseUrl = String.format(
                    "%s/service/rest/v1/components?repository=%s",
                    node.getUrlConfig().prefixUrl(), node.getUrlConfig().getRepository());

            final var res = new ListComponentResponse();
            res.setItems(new ArrayList<>());

            final var digest = node.getCredentials().getBasicAuthenticationDigest();
            ListComponentResponse page = null;

            do {
                var url = baseUrl +
                        (page == null || page.getContinuationToken() == null ?
                                "" :
                                "&continuationToken=" + page.getContinuationToken());

                if (LOG.isDebugEnabled()) {
                    LOG.debug("GET " + url);
                }

                var builder = HttpRequest.newBuilder().uri(URI.create(url))
                        .timeout(Duration.ofMinutes(1));
                if (digest != null) {
                    builder = builder.header("Authorization", "Basic " + digest);
                }

                var httpRequest = builder.build();

                HttpResponse.BodyHandler<String> asString = HttpResponse.BodyHandlers.ofString();

                final var httpResponse = client.send(httpRequest, asString);

                final var body = httpResponse.body();

                if (LOG.isTraceEnabled()) {
                    LOG.debug("GET " + url + " returned " + body);
                }

                page = new Gson().fromJson(body, ListComponentResponse.class);

                for (final Component c: page.getItems()) {
                    c.assets(
                            FilterUtils
                                    .deployableAssets(FilterUtils.toDecoratedAssetCollection(c, c.getAssets()))
                                    .parallelStream()
                                    .map(asset -> (Asset)asset)
                                    .collect(Collectors.toList()));
                }

                if (page.getItems() != null) {
                    res.getItems().addAll(page.getItems());
                }

                if (res.getItems().size() >= 10) {
                    break;
                }
            } while (page.getContinuationToken() != null);

            return res;
        } catch (Throwable t) {
            throw new ReplicatorException(t.getMessage(), t);
        }
    }

    @Override
    public void upload(Node source, Node dest, Component component) throws ReplicatorException {
        LOG.info(
                String.format(
                        "Uploading %s:%s:%s on %s/{%s} to %s/{%s}.",
                        component.getGroup(),
                        component.getName(),
                        component.getVersion(),
                        source.getUrlConfig().prefixUrl(),
                        source.getUrlConfig().getRepository(),
                        dest.getUrlConfig().prefixUrl(),
                        dest.getUrlConfig().getRepository()));

        try {
            final var boundary = "------------------------" +
                    Base64.getEncoder().encodeToString(new Date().toString().getBytes()).substring(0, 16);

            var entityBuilder = MultipartEntityBuilder.create()
                    .addPart("maven2.groupId", new StringBody(component.getGroup(), ContentType.TEXT_PLAIN))
                    .addPart("maven2.artifactId", new StringBody(component.getName(), ContentType.TEXT_PLAIN))
                    .addPart("maven2.version", new StringBody(component.getVersion(), ContentType.TEXT_PLAIN))
                    .setBoundary(boundary);

            int count = 0;
            for (DecoratedAsset asset: FilterUtils.toDecoratedAssetCollection(component, component.getAssets())) {
                count++;

                String downloadUrl = asset.getDownloadUrl();
                byte[] data = HttpUtils.getAsByteArray(downloadUrl, source.getCredentials());

                entityBuilder = entityBuilder
                        .addPart(
                                "maven2.asset" + count,
                                new ByteArrayBody(data, new File(new URL(downloadUrl).getFile()).getName()));

                if (asset.getExtension() != null) {
                    entityBuilder = entityBuilder
                            .addPart(
                                    "maven2.asset" + count + ".extension",
                                    new StringBody(asset.getExtension(), ContentType.TEXT_PLAIN));
                }

                if (asset.getClassifier() != null) {
                    entityBuilder = entityBuilder
                            .addPart(
                                    "maven2.asset" + count + ".classifier",
                                    new StringBody(asset.getClassifier(), ContentType.TEXT_PLAIN));
                }
           }

            var entity = entityBuilder.build();

            /*
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            entity.writeTo(baos);
            InputStream in = new ByteArrayInputStream(baos.toByteArray());

            int nread;
            byte[] buf = new byte[256];

            while ((nread = in.read(buf)) != -1) {
                System.out.print(new String(buf, 0, nread));
            }*/

            final var url = String.format(
                    "%s/service/rest/v1/components?repository=%s",
                    dest.getUrlConfig().prefixUrl(), dest.getUrlConfig().getRepository());

            final var baos = new ByteArrayOutputStream();
            entity.writeTo(baos);

            var digest = dest.getCredentials().getBasicAuthenticationDigest();
            var builder = HttpRequest.newBuilder().uri(URI.create(url))
                    .timeout(Duration.ofMinutes(1))
                    .POST(HttpRequest.BodyPublishers.ofInputStream(() -> new ByteArrayInputStream(baos.toByteArray())))
                    .header("Content-Type", ContentType.MULTIPART_FORM_DATA.getMimeType() + "; boundary=" + boundary);

            if (digest != null) {
                builder = builder.header("Authorization", "Basic " + digest);
            }

            var httpRequest = builder.build();

            HttpResponse.BodyHandler<String> asString = HttpResponse.BodyHandlers.ofString();

            var httpResponse2 = client.send(httpRequest, asString);

            if (httpResponse2.statusCode() < 200 || httpResponse2.statusCode() >= 300) {
                // 400
                // [{"id":"groupId","message":"Missing required component field 'Group ID'"},{"id":"artifactId","message":"Missing required component field 'Artifact ID'"},{"id":"version","message":"Missing required component field 'Version'"},{"id":"extension","message":"Missing required asset field 'Extension' on '1'"}]
                throw new HttpException(
                        String.format(
                                "Uploading %s:%s:%s to %s returned status %d with message %s",
                                component.getGroup(),
                                component.getName(),
                                component.getVersion(),
                                dest.getUrlConfig().prefixUrl(),
                                httpResponse2.statusCode(),
                                httpResponse2.body()),
                        httpResponse2.body(),
                        httpResponse2.statusCode());
            }
        } catch (ReplicatorException e) {
            throw e;
        } catch (Throwable t) {
            throw new ReplicatorException(t.getMessage(), t);
        }
    }

}
