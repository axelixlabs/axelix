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

import { getUserOrigin } from "@/helpers";
import { EUserOrigin, type IUser } from "@/models";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Currently selected users
     */
    selectedUsers: IUser[];

    /**
     * Called when the selected users should be suspended
     */
    onSuspend?: (users: IUser[]) => void;

    /**
     * Called when the selected users should be deleted
     */
    onDelete?: (users: IUser[]) => void;

    /**
     * Clears the selection
     */
    onClear: () => void;
}

export const SelectionBar = ({ selectedUsers, onSuspend, onDelete, onClear }: IProps) => {
    const { t } = useTranslation();

    const localCount = selectedUsers.filter((user) => getUserOrigin(user) === EUserOrigin.LOCAL).length;

    return (
        <>
            <div className={styles.MainWrapper}>
                <span className={styles.Count}>{t("Users.Selection.selected", { count: selectedUsers.length })}</span>
                <span className={`TextUltraSmall ${styles.Split}`}>
                    {t("Users.originSplit", { oidc: selectedUsers.length - localCount, local: localCount })}
                </span>

                <div className={styles.Actions}>
                    <Button size="small" onClick={() => onSuspend?.(selectedUsers)}>
                        {t("Users.Selection.suspend")}
                    </Button>
                    <Button size="small" danger onClick={() => onDelete?.(selectedUsers)}>
                        {t("Users.Selection.delete")}
                    </Button>
                    <Button size="small" type="text" className={styles.Clear} onClick={onClear}>
                        {t("Users.Selection.clear")}
                    </Button>
                </div>
            </div>
        </>
    );
};
