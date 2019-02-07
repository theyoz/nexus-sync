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
package me.yoram.apps.nexus.replicator.config;

import me.yoram.apps.nexus.replicator.api.Bean;

import java.util.Base64;

/**
 * @author Yoram Halberstam (yoram dot halberstam at gmail dot com)
 * @since 07/02/19
 */
public class Credentials extends Bean {
    private String user;
    private String password;

    public String getUser() {
        return user;
    }

    public Credentials user(final String user) {
        this.user = user;

        return this;
    }

    public String getPassword() {
        return password;
    }

    public Credentials password(final String password) {
        this.password = password;

        return this;
    }

    public String getBasicAuthenticationDigest() {
        if (user == null) {
            return null;
        }

        return Base64.getEncoder().encodeToString((user + ":" + (password == null ? "" : password)).getBytes());
    }
}
