package com.nucleonforge.axile.master.api;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nucleonforge.axile.master.api.request.LoginRequest;
import com.nucleonforge.axile.master.api.response.UserProfileResponse;

/**
 * The API for working with users.
 *
 * @author Mikhail Polivakha
 */
@RestController
@RequestMapping(path = ApiPaths.UsersApi.MAIN)
public class UserApi {

    /**
     * Login the user.
     *
     * @param loginRequest request for login
     * @return the HTTP Response with the Authorization header
     */
    @PostMapping(path = ApiPaths.UsersApi.LOGIN)
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        // TODO: handle login logic later
        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer TOKEN")
                .build();
    }

    /**
     * Extracts the profile from the HTTP Request.
     *
     * @return the profile of the given user.
     */
    public UserProfileResponse getProfile(HttpServletRequest request) {
        throw new UnsupportedOperationException();
    }
}
