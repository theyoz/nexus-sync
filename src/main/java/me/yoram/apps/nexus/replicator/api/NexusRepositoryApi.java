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

import com.google.gson.Gson;
import me.yoram.apps.nexus.replicator.config.Config;
import me.yoram.apps.nexus.replicator.config.Node;
import me.yoram.apps.nexus.replicator.dto.Asset;
import me.yoram.apps.nexus.replicator.dto.Component;
import me.yoram.apps.nexus.replicator.dto.ListComponentResponse;
import me.yoram.apps.nexus.replicator.exceptions.ReplicatorException;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;

/**
 * @author Yoram Halberstam (yoram dot halberstam at gmail dot com)
 * @since 05/02/19
 */
public interface NexusRepositoryApi {
    ListComponentResponse getAllComponents(Node node) throws ReplicatorException;
    void upload(Node source, Node dest, Component component) throws ReplicatorException;
}
