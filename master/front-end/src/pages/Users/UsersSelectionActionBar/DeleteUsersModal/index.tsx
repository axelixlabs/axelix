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

export const DeleteUsersAction = () => {
    const { t } = useTranslation();
    const [isModalOpen, setIsModalOpen] = useState<boolean>(false);

    const modalSubtitle = true
        ? "The user records, their credentials, role assignments and activity history are removed from Axelix. Usually the right action once someone has left the company."
        : "2 of them sign in through the OIDC provider. For those, Axelix can only remove what it stores itself: the user record, role assignments and activity history.";

    const modalOkText = true ? "Delete anyway" : "Delete users"
    const modalFooterExtra = true ? "2 seats released" : "Cannot be undone"

    return (
        <>
            <Button className={styles.DeleteButton} danger onClick={() => setIsModalOpen(true)}>
                {t("Users.SelectionActionBar.delete")}
            </Button>

            <UniversalModal
                title="Delete 10 users?"
                subtitle={modalSubtitle}
                open={isModalOpen}
                onOk={() => setIsModalOpen(false)}
                onClose={() => setIsModalOpen(false)}
                okText={modalOkText}
                footerExtra={<div className={`TextSmall ${styles.FooterExtra}`}>{modalFooterExtra}</div>}
            >
                {true ? (
                    <div className={styles.ModalContentWrapper}>
                        <AppAlert type="info" title="Dashboards and wallboards they created stay in place and become unowned." />
                        <AppAlert type="error" title="Nothing is recoverable afterwards. Suspend instead if you may need the account or its history again." />
                    </div>
                ) : (
                    <div className={styles.ModalContentWrapper}>
                        <AppAlert type="error" title="The accounts still exist at the provider, so these users can sign in again and Axelix will create them anew, without roles. Remove them at the provider first, then here." />
                        <AppAlert type="info" title="The 1 local user is deleted outright, credentials included." />
                    </div>
                )}
            </UniversalModal>
        </>
    );
};
