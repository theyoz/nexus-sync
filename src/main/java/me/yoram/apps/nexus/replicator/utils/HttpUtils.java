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

import me.yoram.apps.nexus.replicator.Replicator;
import me.yoram.apps.nexus.replicator.config.Credentials;
import me.yoram.apps.nexus.replicator.exceptions.HttpException;
import me.yoram.apps.nexus.replicator.exceptions.ReplicatorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

/**
 * @author Yoram Halberstam (yoram dot halberstam at gmail dot com)
 * @since 07/02/19
 */
public class HttpUtils {
    private static final Logger LOG = LoggerFactory.getLogger(HttpUtils.class);

    private static final HttpClient CLIENT = HttpClient
            .newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .proxy(ProxySelector.getDefault())
            .build();

    public static void get(
            final String url, final Credentials credentials, final OutputStream out) throws HttpException {
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Downloading %s", url));
            }
            var digest = credentials == null ? null : credentials.getBasicAuthenticationDigest();
            var builder = HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofMinutes(10));

            if (digest != null) {
                builder = builder.header("Authorization", "Basic " + digest);
            }

            var httpRequest = builder.build();
            HttpResponse.BodyHandler<byte[]> asFile = HttpResponse.BodyHandlers.ofByteArray();

            var httpResponse = CLIENT.send(httpRequest, asFile);

            if (httpResponse.statusCode() < 200 || httpResponse.statusCode() > 299) {
                throw new HttpException(
                        "Error during GET operation", new String(httpResponse.body()), httpResponse.statusCode());
            }

            out.write(httpResponse.body());

            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Downloaded %s", url));
            }
        } catch (HttpException e) {
            throw e;
        } catch (Throwable t) {
            throw new HttpException(t.getMessage(), t);
        }
    }

    public static byte[] getAsByteArray(final String url, final Credentials credentials) throws HttpException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        get(url, credentials, baos);

        return baos.toByteArray();
    }

    public static File getAsFile(final String url, final Credentials credentials) throws HttpException {
        try {
            final URL u = new URL(url);
            final File f = File.createTempFile("HttpUtils", "." + new File(u.getFile()).getName());

            try (final OutputStream out = new FileOutputStream(f)) {
                get(url, credentials, out);
            }

            return f;
        } catch (HttpException e) {
            throw  e;
        } catch (Throwable t) {
            throw new HttpException(t.getMessage(), t);
        }
    }
}
