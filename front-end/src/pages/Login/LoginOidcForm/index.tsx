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
import { Button } from "antd";
import { useTranslation } from "react-i18next";

import { type OIDCAuthOption } from "models";
import { authorize } from "services";

import styles from "./styles.module.css";

import { LockIcon } from "assets";

interface IProps {
    option: OIDCAuthOption;
}

export const LoginOidcForm = ({ option }: IProps) => {
    const { t } = useTranslation();

    return (
        <>
            <Button
                htmlType="submit"
                icon={<LockIcon />}
                className={styles.SubmitButton}
                onClick={() => authorize(option)}
            >
                {t("Authentication.oidcForm.loginButtonText")}
            </Button>
        </>
    );
};
