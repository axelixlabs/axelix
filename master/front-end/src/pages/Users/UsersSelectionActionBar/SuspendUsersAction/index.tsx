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
import { useState } from "react";
import { useTranslation } from "react-i18next";

import { UniversalModal, AppAlert } from "@/components";

import styles from "./styles.module.css";

export const SuspendUsersAction = () => {
    const { t } = useTranslation();
    const [isModalOpen, setIsModalOpen] = useState<boolean>(false);

    const firstAlertText = true ? "Accounts, roles and recorded activity history are kept as they are." : "They will not be able to sign in to Axelix, from either the OIDC provider or a local password."
    const secondAlertText = "A suspended user can be reactivated at any time, or deleted later."

    return (
        <>
            <Button className={styles.SuspendButton} onClick={() => setIsModalOpen(true)}>
                {t("Users.SelectionActionBar.suspend")}
            </Button>

            <UniversalModal
                title="Suspend 3 users?"
                subtitle="They will not be able to sign in to Axelix, from either the OIDC provider or a local password."
                open={isModalOpen}
                onOk={() => setIsModalOpen(false)}
                onClose={() => setIsModalOpen(false)}
                okText={"Suspend users"}
                okButtonStyle={{ backgroundColor: "var(--axelix-primary-black)", borderColor: "var(--axelix-primary-black)" }}
                okButtonClassName={styles.SuspendOkButton}
                footerExtra={<div className={`TextUltraSmall ${styles.FooterExtra}`}>Seats stay allocated</div>}
            >
                <div className={styles.ModalContentWrapper}>
                    <AppAlert title={firstAlertText} type="info" />

                    <AppAlert title={secondAlertText} type="info" />
                </div>
            </UniversalModal>
        </>
    );
};
