/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.axelixlabs.axelix.e2e.client;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.Cookie;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.cookie.CookieFilter;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.jspecify.annotations.Nullable;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

/**
 * REST API client for interacting with the Axelix Master during E2E testing.
 *
 * @author Nikita Kirillov
 */
public class AxelixMasterApiClient {

    private static final String EXTERNAL_API_BASE_PATH = "/api/external";

    private final String baseUrl;
    private final CookieFilter cookieFilter = new CookieFilter();

    public AxelixMasterApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * Login via the local/super-admin login flow ({@code POST /api/external/users/login}) and
     * stores the returned auth cookie in the {@link #cookieFilter} for subsequent requests.
     */
    public void login(String username, String password) {
        requestSpec()
                .body("""
                        {"username": "%s", "password": "%s"}
                        """.formatted(username, password))
                .post(EXTERNAL_API_BASE_PATH + "/users/login")
                .then()
                .statusCode(200);
    }

    /**
     * Logout and clean auth cookie in the {@link #cookieFilter}.
     */
    public void logout() {
        requestSpec().post(EXTERNAL_API_BASE_PATH + "/users/logout");
    }

    public void registerLocalUser(String username, @Nullable String email, String password, String role) {
        // Using a Map instead of text blocks because the email can be null.
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("username", username);
        body.put("email", email);
        body.put("password", password);
        body.put("role", role);

        requestSpec()
                .body(body)
                .post(EXTERNAL_API_BASE_PATH + "/users-management/create")
                .then()
                .statusCode(201);
    }

    public String getUserId(String username) {
        return findUserId(username).orElseThrow(() -> new AssertionError("User '" + username + "' was not found"));
    }

    public Optional<String> findUserId(String username) {
        List<Map<String, Object>> users = requestSpec()
                .get(EXTERNAL_API_BASE_PATH + "/users/feed")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList("$");

        return users.stream()
                .filter(user -> username.equals(user.get("username")))
                .map(user -> (String) user.get("id"))
                .findFirst();
    }

    public void updateUsersStatus(List<String> userIds, String status) {
        String ids = userIds.stream().map("\"%s\""::formatted).collect(Collectors.joining(", "));

        requestSpec()
                .body("""
                        {"ids": [%s], "status": "%s"}
                        """.formatted(ids, status))
                .put(EXTERNAL_API_BASE_PATH + "/users-management/status")
                .then()
                .statusCode(204);
    }

    public void verifySuspendedUserCannotLogin(String username, String password) {
        requestSpec()
                .body("""
                        {"username": "%s", "password": "%s"}
                        """.formatted(username, password))
                .post(EXTERNAL_API_BASE_PATH + "/users/login")
                .then()
                .statusCode(403)
                .body("errorCode", equalTo("USER_SUSPENDED"));
    }

    public void verifySuspendedUserCannotLoginViaOAuth2(String username, String password) {
        try (Playwright playwright = Playwright.create();
                Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true))) {

            Page page = browser.newPage();
            page.navigate(buildOAuth2LoginRequestUrl());
            page.fill("#username", username);
            page.fill("#password", password);

            Response response = page.waitForResponse(
                    candidate -> candidate.url().contains(EXTERNAL_API_BASE_PATH + "/oauth2/callback"),
                    () -> page.click("#kc-login"));

            if (response.status() != 403 || !response.text().contains("USER_SUSPENDED")) {
                throw new AssertionError("Expected suspended OIDC login to return 403 USER_SUSPENDED, but got "
                        + response.status() + ": " + response.text());
            }
        } catch (Exception e) {
            throw new AssertionError("OAuth2/Playwright: " + e.getMessage(), e);
        }
    }

    /**
     * Returns the names of every instance currently known to Master, as surfaced by the wallboard
     * grid endpoint ({@code GET /api/external/applications/grid}).
     */
    public Set<String> getRegisteredInstanceNames() {
        List<String> names = requestSpec()
                .get(EXTERNAL_API_BASE_PATH + "/applications/grid")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList("instances.name", String.class);

        return names.stream().collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Login via the OAuth2/OIDC login flow and stores the returned auth cookie
     * in the {@link #cookieFilter} for subsequent requests.
     */
    public void loginViaOAuth2(String username, String password) {
        try (Playwright playwright = Playwright.create();
                Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true))) {

            BrowserContext context = browser.newContext();
            Page page = context.newPage();

            String oAuth2LoginRequestUrl = buildOAuth2LoginRequestUrl();

            page.navigate(oAuth2LoginRequestUrl);

            page.fill("#username", username);
            page.fill("#password", password);

            page.click("#kc-login");

            page.waitForURL("**/wallboard");

            for (Cookie cookie : context.cookies()) {
                if ("auth_token".equals(cookie.name) || "authorities".equals(cookie.name)) {
                    BasicClientCookie apacheCookie = new BasicClientCookie(cookie.name, cookie.value);

                    apacheCookie.setDomain("master");
                    apacheCookie.setPath("/");

                    cookieFilter.getCookieStore().addCookie(apacheCookie);
                }
            }
        } catch (Exception e) {
            throw new AssertionError("OAuth2/Playwright: " + e.getMessage(), e);
        }
    }

    private String buildOAuth2LoginRequestUrl() {
        JsonPath settings = getAuthSettings();
        List<Map<String, Object>> options = settings.getList("authenticationOptions");
        Map<String, Object> oidcOption = options.stream()
                .filter(option -> "oidc".equals(option.get("type")))
                .findFirst()
                .orElseThrow(
                        () -> new AssertionError("No 'oauth2/oidc' authentication option in '/settings' response"));

        String authorizationEndpoint = (String) oidcOption.get("authorizationEndpoint");
        String clientId = (String) oidcOption.get("clientId");
        String redirectUri = (String) oidcOption.get("redirectUri");
        String scope = (String) oidcOption.get("scope");

        return authorizationEndpoint + "?"
                + "response_type=code"
                + "&client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&scope=" + scope;
    }

    /**
     * Returns the raw {@code GET /api/external/settings}.
     */
    private JsonPath getAuthSettings() {
        return requestSpec().get(EXTERNAL_API_BASE_PATH + "/settings").jsonPath();
    }

    private RequestSpecification requestSpec() {
        return given().filter(cookieFilter)
                .spec(new RequestSpecBuilder()
                        .setBaseUri(baseUrl)
                        .setContentType("application/json")
                        .build());
    }
}
