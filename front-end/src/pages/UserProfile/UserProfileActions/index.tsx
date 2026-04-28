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
import {App, Button, Modal} from "antd";
import {useState} from "react";
import {useTranslation} from "react-i18next";
import {useNavigate} from "react-router";

import {deleteUser} from "services";

import styles from "./styles.module.css";

import {TrashIcon} from "assets";

interface IProps {
    /**
     * Unique identifier of the user
     */
    userId: string;
}

export const UserProfileActions = ({userId}: IProps) => {
    const {t} = useTranslation();
    const {message} = App.useApp();

    const navigate = useNavigate();
    const [isModalOpen, setIsModalOpen] = useState<boolean>(false);

    const showModal = (): void => {
        setIsModalOpen(true);
    };

    const handleOk = (): void => {
        deleteUser(userId)
            .then(() => {
                message.success(t("Users.userDeleted"));
                navigate("/users");
            })
            .finally(() => {
                setIsModalOpen(false);
            })
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
                centered
            >
                {t("Users.deleteModalDescription")}
            </Modal>

            <Button type="primary" icon={<TrashIcon/>} onClick={showModal} className={styles.DeleteUser}>
                {t("Users.deleteUser")}
            </Button>

            <Button disabled>{t("Users.accessLog")}</Button>
        </div>
    );
};
