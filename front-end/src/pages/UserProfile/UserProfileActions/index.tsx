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
import { useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router";

import { extractErrorCode } from "helpers";
import { StatelessRequest } from "models";
import { deleteUser } from "services";

import styles from "./styles.module.css";

import { TrashIcon } from "assets";

interface IProps {
    /**
     * Unique identifier of the user
     */
    userId: string;
}

export const UserProfileActions = ({ userId }: IProps) => {
    const { t } = useTranslation();
    const { message } = App.useApp();

    const navigate = useNavigate();
    const [isModalOpen, setIsModalOpen] = useState<boolean>(false);
    const [requestData, setRequestData] = useState(StatelessRequest.inactive());

    const showModal = (): void => {
        setIsModalOpen(true);
    };

    const handleOk = (): void => {
        setRequestData(StatelessRequest.loading());

        deleteUser(userId)
            .then(() => {
                setRequestData(StatelessRequest.success());
                message.success(t("Users.userDeleted"));
                setIsModalOpen(false);
                navigate("/users");
            })
            .catch((error) => {
                setRequestData(StatelessRequest.error(extractErrorCode(error?.response?.data)));
            });
    };

    const handleCancel = (): void => {
        setIsModalOpen(false);
    };

    return (
        <div className={styles.MainWrapper}>
            <Modal
                title={t("Users.deleteUser")}
                open={isModalOpen}
                onOk={handleOk}
                onCancel={handleCancel}
                loading={requestData.loading}
                centered
            >
                {t("Users.deleteModalDescription")}
            </Modal>

            <Button type="primary" icon={<TrashIcon />} onClick={showModal} className={styles.DeleteUser}>
                {t("Users.deleteUser")}
            </Button>

            <Button disabled>{t("Users.accessLog")}</Button>
        </div>
    );
};
