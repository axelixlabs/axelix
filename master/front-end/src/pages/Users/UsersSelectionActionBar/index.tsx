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

import { DeleteUsersAction } from "./DeleteUsersModal";
import { SuspendUsersAction } from "./SuspendUsersAction";
import styles from "./styles.module.css";

export const UsersSelectionActionBar = () => {
    const { t } = useTranslation();

    return (
        <>
            <div className={styles.MainWrapper}>
                <div className={styles.InfoWrapper}>
                    <span className={styles.SelectedUsers}>
                        {t("Users.SelectionActionBar.selectedUsers", { count: 10 })}
                    </span>
                    <span className={`TextSmall ${styles.OidcLocal}`}>
                        {t("Users.SelectionActionBar.oidcLocal", { oidcCount: 2, localCount: 1 })}
                    </span>
                </div>

                <div className={styles.ActionsButtons}>
                    <SuspendUsersAction />

                    <DeleteUsersAction />

                    {/* TODO: Clear selected users  */}
                    <Button type="text" className={styles.ClearButton}>
                        {t("Users.SelectionActionBar.clear")}
                    </Button>
                </div>
            </div>
        </>
    );
};
