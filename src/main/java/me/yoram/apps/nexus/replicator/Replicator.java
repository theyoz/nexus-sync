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

import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;

/**
 * @author Yoram Halberstam (yoram dot halberstam at gmail dot com)
 * @since 05/02/19
 */
public class Replicator {
    public static void main(String[] args) throws Exception {
        final var user = args[0];
        final var pass = args[1];

        final var digest = Base64.getEncoder().encodeToString((user + ":" + pass).getBytes());

        HttpClient HTTP_CLIENT = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.NORMAL).proxy(ProxySelector.getDefault()).build();

        var HTTP_REQUEST = HttpRequest.newBuilder()
                .uri(URI.create("https://nexus.yoram.me/service/rest/v1/components?repository=maven-releases"))
                .timeout(Duration.ofMinutes(1))
                //.header("Content-Type", "application/json")
                .header("Authorization", "Basic " + digest)
                .build();

        HttpResponse.BodyHandler<String> asString = HttpResponse.BodyHandlers.ofString();

        var HTTP_RESPONSE = HTTP_CLIENT.send(HTTP_REQUEST, asString);

        System.out.println(HTTP_RESPONSE.statusCode() + ": " + HTTP_RESPONSE.body());
    }
}
