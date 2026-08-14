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

import styles from "./styles.module.css";

export const UsersSelectionActionBar = () => {
    const { t } = useTranslation();

    // TODO: Remove this mock data in the future
    const selectedCount = 10;
    const oidcCount = 2;
    const localCount = 1;

    if (!selectedCount) {
        return null;
    }

    return (
        <>
            <div className={styles.MainWrapper}>
                <div className={styles.InfoWrapper}>
                    <span className={styles.SelectedUsers}>
                        {t("Users.SelectionActionBar.selectedUsers", { count: selectedCount })}
                    </span>
                    <span className={`TextSmall ${styles.OidcLocal}`}>
                        {t("Users.SelectionActionBar.oidcLocal", { oidcCount, localCount })}
                    </span>
                </div>

                <div className={styles.ActionsButtons}>
                    <Button className={styles.SuspendButton} onClick={() => {}}>
                        {t("Users.SelectionActionBar.suspend")}
                    </Button>
                    <Button className={styles.DeleteButton} danger onClick={() => {}}>
                        {t("Users.SelectionActionBar.delete")}
                    </Button>
                    <Button type="text" className={styles.ClearButton} onClick={() => {}}>
                        {t("Users.SelectionActionBar.clear")}
                    </Button>
                </div>
            </div>
        </>
    );
};
