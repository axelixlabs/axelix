package com.nucleonforge.axile.master.api;

/**
 * Constant class that holds the paths to the APIs that Axile Master exposes
 *
 * @author Mikhail Polivakha
 */
public final class ApiPaths {

    private ApiPaths() {}

    public static final class UsersApi {

        public static final String MAIN = "/users";

        public static final String LOGIN = "/login";
    }

    public static final class ApplicationApi {

        public static final String MAIN = "/applications";

        public static final String GRID = "/grid";

        public static final String SINGLE = "/single/{name}";
    }
}
