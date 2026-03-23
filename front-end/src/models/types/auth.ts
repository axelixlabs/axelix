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
import { type LOGIN_PASSWORD_AUTH_OPTION_TYPE_NAME, OIDC_AUTH_OPTION_TYPE_NAME } from "utils/auth";

export type LoginPasswordAuthOption = {
    type: typeof LOGIN_PASSWORD_AUTH_OPTION_TYPE_NAME;
};

export type OIDCAuthOption = {
    type: typeof OIDC_AUTH_OPTION_TYPE_NAME;

    /**
     * The scope with which the /authorize endpoint of the OIDC provider must be hit.
     */
    scope: string;

    /**
     * The client-id of the Axelix as the client of the external OIDC provider.
     */
    clientId: string;

    /**
     * The redirect-uri onto which the OIDC provider must redirect with the authorization_code.
     */
    redirectUri: string;

    /**
     * The URL of the /authorize on the OIDC provider side.
     */
    authorizationEndpoint: string;
};

/**
 * Possible auth option from the backend.
 */
export type AuthOption = LoginPasswordAuthOption | OIDCAuthOption;
