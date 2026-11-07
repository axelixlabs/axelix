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
import { App, Modal } from "antd";
import { useState } from "react";
import { useTranslation } from "react-i18next";

import { extractErrorCode, showErrorNotification } from "@/helpers";
import { EUserStatus, type IUser, StatelessRequest } from "@/models";
import { updateUsersStatus } from "@/services";

import styles from "./styles.module.css";

interface IProps {
    /**
     * User to suspend or reactivate
     */
    user: IUser;

    /**
     * Name used in the dialog copy
     */
    displayName: string;

    /**
     * Whether the dialog is open
     */
    open: boolean;

    /**
     * Closes the dialog without changing the user
     */
    onClose: () => void;

    /**
     * Reloads the profile after the status change
     */
    onSaved: () => void;
}

export const SuspendUser = ({ user, displayName, open, onClose, onSaved }: IProps) => {
    const { t } = useTranslation();
    const { message } = App.useApp();
    const [requestData, setRequestData] = useState(StatelessRequest.inactive());
    const canSignIn = user.status !== EUserStatus.SUSPENDED;

    const confirm = (): void => {
        const nextStatus = canSignIn ? EUserStatus.SUSPENDED : EUserStatus.ACTIVE;
        setRequestData(StatelessRequest.loading());

        updateUsersStatus([user.id], nextStatus)
            .then(() => {
                setRequestData(StatelessRequest.success());
                message.success(t(canSignIn ? "Users.Profile.userSuspended" : "Users.Profile.userReactivated"));
                onClose();
                onSaved();
            })
            .catch((error) => {
                const errorCode = extractErrorCode(error?.response?.data);
                setRequestData(StatelessRequest.error(errorCode));
                showErrorNotification(errorCode);
            });
    };

    return (
        <Modal
            open={open}
            onCancel={onClose}
            onOk={confirm}
            centered
            confirmLoading={requestData.loading}
            okText={t(canSignIn ? "Users.Profile.suspendConfirm" : "Users.Profile.reactivateConfirm")}
            cancelText={t("cancel")}
            okButtonProps={canSignIn ? { danger: true } : undefined}
            title={t(canSignIn ? "Users.Profile.suspendTitle" : "Users.Profile.reactivateTitle", { name: displayName })}
        >
            <p>{t(canSignIn ? "Users.Profile.suspendBody" : "Users.Profile.reactivateBody", { name: displayName })}</p>
            {canSignIn && (
                <div className={styles.SuspendNotes}>
                    <p>{t("Users.Profile.suspendKept")}</p>
                    <p>{t("Users.Profile.suspendLater")}</p>
                </div>
            )}
        </Modal>
    );
};
