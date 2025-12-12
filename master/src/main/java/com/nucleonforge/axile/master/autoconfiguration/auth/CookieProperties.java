/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axile.master.autoconfiguration.auth;

import org.jspecify.annotations.Nullable;

/**
 * Cookie configuration properties.
 *
 * @since 11.12.2025
 * @author Nikita Kirillov
 */
public class CookieProperties {

    private String name = "auth_token";

    /**
     * SameSite attribute for cookies.
     */
    private String sameSite = "Strict";

    /**
     * Domain attribute for cookies.
     */
    @Nullable
    private String domain;

    /**
     * Secure attribute for cookies.
     * <p><b>Default:</b> true</p>
     */
    private boolean secure = true;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSameSite() {
        return sameSite;
    }

    public void setSameSite(String sameSite) {
        this.sameSite = sameSite;
    }

    @Nullable
    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }
}
