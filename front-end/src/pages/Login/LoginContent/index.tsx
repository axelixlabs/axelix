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
import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";

import { Loader } from "components";
import { fetchData } from "helpers";
import { type IAuthOptionsResponseBody, type OIDCAuthOption, StatefulRequest } from "models";
import { getAuthOptions } from "services";
import { LOGIN_PASSWORD_AUTH_OPTION_TYPE_NAME, OIDC_AUTH_OPTION_TYPE_NAME } from "utils";

import { LoginOidcForm } from "../LoginOidcForm";

import styles from "./styles.module.css";
import { LoginPasswordForm } from "../LoginPasswordForm";

/**
 * Function that is similar to kotlin's let - apply transformation on an
 * arbitrary value if the value is there (not undefied).
 */
function ifFound<I, O>(value: I | undefined, transformer: (val: I) => O): O | undefined {
    if (value) {
        return transformer(value);
    } else {
        return undefined;
    }
}

export const LoginContent = () => {
    const { t } = useTranslation();

    const [authOptions, setAuthOptions] = useState(StatefulRequest.loading<IAuthOptionsResponseBody>());

    useEffect(() => {
        fetchData(setAuthOptions, getAuthOptions);
    }, []);

    if (authOptions.loading) {
        return <Loader />;
    }

    if (authOptions.error) {
        // TODO: How do we handle errors in this case?
    }

    const response = authOptions.response!;

    const getAuthOption = (optionName: string) => {
        return response.authProviders.find((value) => optionName === value.type);
    };

    return (
        <>
            <div className={styles.MainWrapper}>
                <h1 className={`TextLarge ${styles.LoginTitle}`}>{t("Authentication.welcome")}</h1>
                {getAuthOption(LOGIN_PASSWORD_AUTH_OPTION_TYPE_NAME) && <LoginPasswordForm />}
                {ifFound(getAuthOption(OIDC_AUTH_OPTION_TYPE_NAME), (value) => (
                    <LoginOidcForm option={value as OIDCAuthOption} />
                ))}
            </div>
        </>
    );
};
