package com.sivalabs.ft.features.api.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public class SecurityUtils {

    public static String getCurrentUsername() {
        var loginUserDetails = getLoginUserDetails();
        var username = loginUserDetails.get("username");
        if (loginUserDetails.isEmpty() || username == null) {
            return null;
        }
        return String.valueOf(username);
    }

    static Map<String, Object> getLoginUserDetails() {
        Map<String, Object> map = new HashMap<>();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            return map;
        }
        Jwt jwt = (Jwt) jwtAuth.getPrincipal();

        map.put("username", jwt.getClaimAsString("preferred_username"));
        map.put("email", jwt.getClaimAsString("email"));
        map.put("name", jwt.getClaimAsString("name"));
        map.put("token", jwt.getTokenValue());
        map.put("authorities", authentication.getAuthorities());
        map.put("roles", getRoles(jwt));

        return map;
    }

    private static List<String> getRoles(Jwt jwt) {
        Map<String, Object> realm_access = (Map<String, Object>) jwt.getClaims().get("realm_access");
        if (realm_access != null && !realm_access.isEmpty()) {
            return (List<String>) realm_access.get("roles");
        }
        return List.of();
    }
}
