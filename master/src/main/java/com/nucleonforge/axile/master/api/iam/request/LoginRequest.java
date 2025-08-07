package com.nucleonforge.axile.master.api.iam.request;

/**
 * Request for login.
 *
 * @author Mikhail Polivakha
 */
public record LoginRequest(String username, String password) {}
