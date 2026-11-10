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
import { App, Button, Modal } from "antd";
import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";

import { InfoIcon } from "@/assets";
import { extractErrorCode, showErrorNotification } from "@/helpers";
import { type IRoleProfile, type IUser, StatelessRequest } from "@/models";
import { editUser } from "@/services";

import { userUpdateFrom } from "../userUpdate";

import { RoleOption } from "./RoleOption";
import styles from "./styles.module.css";

interface IProps {
    /**
     * User whose roles are being edited
     */
    user: IUser;

    /**
     * Every role that can be granted, from the roles feed
     */
    roles: IRoleProfile[];

    /**
     * Name used in the dialog title
     */
    displayName: string;

    /**
     * Given name used in the dialog copy
     */
    givenName: string;

    /**
     * Whether the dialog is open
     */
    open: boolean;

    /**
     * Closes the dialog without saving
     */
    onClose: () => void;

    /**
     * Reloads the profile after the roles are saved
     */
    onSaved: () => void;
}

const roleOptions = (roles: readonly IRoleProfile[], held: readonly string[]): IRoleProfile[] => {
    const known = new Set(roles.map((role) => role.name));
    const missing = held
        .filter((name) => !known.has(name))
        .map((name) => ({ id: name, name, membersCount: 0, description: "" }));

    return [...roles, ...missing];
};

export const ChangeRoles = ({ user, roles, displayName, givenName, open, onClose, onSaved }: IProps) => {
    const { t } = useTranslation();
    const { message } = App.useApp();
    const [selected, setSelected] = useState<string[]>(user.roles);
    const [requestData, setRequestData] = useState(StatelessRequest.inactive());
    const options = roleOptions(roles, user.roles);
    const held = new Set<string>(user.roles);

    useEffect(() => {
        if (open) {
            setSelected(user.roles);
        }
    }, [open, user]);

    const added = options.filter((role) => selected.includes(role.name) && !held.has(role.name));
    const removed = options.filter((role) => !selected.includes(role.name) && held.has(role.name));
    const unchanged = added.length === 0 && removed.length === 0;

    const toggle = (role: string): void => {
        setSelected((current) =>
            current.includes(role) ? current.filter((item) => item !== role) : [...current, role],
        );
    };

    const save = (): void => {
        if (selected.length === 0) {
            return;
        }

        setRequestData(StatelessRequest.loading());

        editUser(userUpdateFrom(user, { roles: selected }))
            .then(() => {
                setRequestData(StatelessRequest.success());
                message.success(t("Users.userEdited"));
                onSaved();
                onClose();
            })
            .catch((error) => {
                setRequestData(StatelessRequest.error(extractErrorCode(error?.response?.data)));
                showErrorNotification(extractErrorCode(error?.response?.data));
            });
    };

    return (
        <Modal
            open={open}
            onCancel={onClose}
            centered
            width={560}
            title={
                <div className={styles.Title}>
                    <span className="TextMedium">{t("Users.Profile.changeRolesTitle", { name: displayName })}</span>
                    <span className={styles.Subtitle}>
                        {t("Users.Profile.changeRolesSubtitle", { name: givenName })}
                    </span>
                </div>
            }
            footer={
                <div className={styles.Footer}>
                    <span className={styles.TakesEffect}>
                        <InfoIcon />
                        {t("Users.Profile.takesEffect", { name: givenName })}
                    </span>
                    <span className={styles.FooterActions}>
                        <Button onClick={onClose}>{t("cancel")}</Button>
                        <Button
                            type="primary"
                            disabled={unchanged || selected.length === 0}
                            loading={requestData.loading}
                            onClick={save}
                        >
                            {t("confirm")}
                        </Button>
                    </span>
                </div>
            }
        >
            <div className={styles.Roles}>
                {options.map((role) => (
                    <RoleOption
                        key={role.id}
                        role={role.name}
                        description={role.description}
                        held={held.has(role.name)}
                        checked={selected.includes(role.name)}
                        onToggle={toggle}
                    />
                ))}
            </div>

            {selected.length === 0 && <p className={styles.RolesRequired}>{t("Users.ValidationErrors.roles")}</p>}
        </Modal>
    );
};
