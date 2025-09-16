package com.nucleonforge.axile.master.api;

import jakarta.servlet.http.HttpServletRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nucleonforge.axile.master.api.error.SimpleApiError;
import com.nucleonforge.axile.master.api.request.LoginRequest;
import com.nucleonforge.axile.master.api.response.UserProfileResponse;

/**
 * The API for working with users.
 *
 * @author Mikhail Polivakha
 */
@Tag(
        name = "API for working with Users",
        description = "The beans endpoint provides information about the application’s beans.")
@RestController
@RequestMapping(path = ApiPaths.UsersApi.MAIN)
public class UserApi {

    /**
     * Login the user.
     *
     * @param loginRequest request for login
     * @return the HTTP Response with the Authorization header
     */
    @Operation(
            summary = "Log-in by the username/password combination",
            responses = {
                @ApiResponse(
                        description = "OK",
                        responseCode = "200",
                        headers = {
                            @Header(
                                    name = "Authorization",
                                    required = true,
                                    description = "The JWT token that should be subsequently used for auth purposes")
                        }),
                @ApiResponse(
                        description = "Bad Request",
                        responseCode = "400",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = SimpleApiError.class))),
                @ApiResponse(
                        description = "Unauthorized. Most likely the credentials pair username/password is wrong",
                        responseCode = "401"),
                @ApiResponse(description = "Forbidden. The access into the system is forbidden", responseCode = "403"),
                @ApiResponse(
                        description = "Internal Server Error",
                        responseCode = "500",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = SimpleApiError.class)))
            })
    @Parameter(name = "loginRequest", description = "Request for login", required = true)
    @PostMapping(path = ApiPaths.UsersApi.LOGIN)
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        throw new UnsupportedOperationException();
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
