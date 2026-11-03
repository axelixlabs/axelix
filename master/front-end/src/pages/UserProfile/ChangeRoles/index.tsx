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
import { App, Button, Checkbox, Modal } from "antd";
import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";

import { InfoIcon } from "@/assets";
import { extractErrorCode, showErrorNotification } from "@/helpers";
import { ERoles, type IUser, StatelessRequest } from "@/models";
import { editUser } from "@/services";

import { PROFILE_ROLES, authorityDelta } from "../permissions";
import { userUpdateFrom } from "../userUpdate";

import styles from "./styles.module.css";

interface IProps {
    user: IUser;
    displayName: string;
    givenName: string;
    open: boolean;
    onClose: () => void;
    onSaved: () => void;
}

export const ChangeRoles = ({ user, displayName, givenName, open, onClose, onSaved }: IProps) => {
    const { t } = useTranslation();
    const { message } = App.useApp();
    const [selected, setSelected] = useState<ERoles[]>(user.roles);
    const [requestData, setRequestData] = useState(StatelessRequest.inactive());

    useEffect(() => {
        if (open) {
            setSelected(user.roles);
        }
    }, [open, user]);

    const added = PROFILE_ROLES.filter((role) => selected.includes(role) && !user.roles.includes(role));
    const removed = PROFILE_ROLES.filter((role) => !selected.includes(role) && user.roles.includes(role));
    const delta = authorityDelta(user.roles, selected);
    const unchanged = added.length === 0 && removed.length === 0;
    const isRevoke = added.length === 0 && removed.length === 1;

    const confirmLabel = (() => {
        if (added.length === 1 && removed.length === 0) {
            return t("Users.Profile.addRole", { role: added[0] });
        }

        if (isRevoke) {
            return t("Users.Profile.revokeRole", { role: removed[0] });
        }

        return t("confirm");
    })();

    const toggle = (role: ERoles): void => {
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
                            danger={isRevoke}
                            disabled={unchanged || selected.length === 0}
                            loading={requestData.loading}
                            onClick={save}
                        >
                            {confirmLabel}
                        </Button>
                    </span>
                </div>
            }
        >
            <div className={styles.Roles}>
                {PROFILE_ROLES.map((role) => {
                    const held = user.roles.includes(role);
                    const checked = selected.includes(role);
                    let state: "current" | "adding" | "revoking" | "plain" = "plain";

                    if (checked && held) {
                        state = "current";
                    } else if (checked) {
                        state = "adding";
                    } else if (held) {
                        state = "revoking";
                    }

                    return (
                        <div
                            key={role}
                            className={`${styles.Role} ${state === "adding" ? styles.Adding : ""} ${
                                state === "revoking" ? styles.Revoking : ""
                            }`}
                            onClick={() => toggle(role)}
                        >
                            <span onClick={(event) => event.stopPropagation()}>
                                <Checkbox checked={checked} onChange={() => toggle(role)} />
                            </span>
                            <span className={styles.RoleText}>
                                <span className={styles.RoleHeader}>
                                    <span className={styles.RoleName}>{role}</span>
                                    {state === "current" && (
                                        <span className={styles.Current}>{t("Users.Profile.current")}</span>
                                    )}
                                    {state === "adding" && (
                                        <span className={styles.AddingLabel}>{t("Users.Profile.adding")}</span>
                                    )}
                                    {state === "revoking" && (
                                        <span className={styles.RevokingLabel}>{t("Users.Profile.revoking")}</span>
                                    )}
                                </span>
                                <span className={styles.RoleDescription}>
                                    {t(`Users.Profile.RoleDescriptions.${role}`)}
                                </span>
                            </span>
                        </div>
                    );
                })}
            </div>

            {selected.length === 0 && <p className={styles.RolesRequired}>{t("Users.ValidationErrors.roles")}</p>}

            {(delta.gained.length > 0 || delta.lost.length > 0) && (
                <div className={styles.Delta}>
                    {delta.gained.length > 0 && (
                        <div>
                            <div className={styles.GainTitle}>
                                {t("Users.Profile.gain", { name: givenName, count: delta.gained.length })}
                            </div>
                            {delta.gained.map((authority) => (
                                <div key={authority} className={styles.GainRow}>
                                    <span>+</span>
                                    <span>
                                        {t(`Users.Profile.Authorities.${authority}`)}
                                        <span className={styles.Code}>{authority}</span>
                                    </span>
                                </div>
                            ))}
                        </div>
                    )}
                    {delta.lost.length > 0 && (
                        <div>
                            <div className={styles.LoseTitle}>
                                {t("Users.Profile.lose", { name: givenName, count: delta.lost.length })}
                            </div>
                            {delta.lost.map((authority) => (
                                <div key={authority} className={styles.LoseRow}>
                                    <span>−</span>
                                    <span>
                                        {t(`Users.Profile.Authorities.${authority}`)}
                                        <span className={styles.Code}>{authority}</span>
                                    </span>
                                </div>
                            ))}
                            {delta.retainedThrough && (
                                <p className={styles.Retained}>
                                    {t("Users.Profile.staysGranted", { role: delta.retainedThrough })}
                                </p>
                            )}
                        </div>
                    )}
                </div>
            )}
        </Modal>
    );
};
