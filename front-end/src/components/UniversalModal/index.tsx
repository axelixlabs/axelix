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
import { Modal } from "antd";
import type { PropsWithChildren } from "react";
import { useTranslation } from "react-i18next";

export interface IProps {
    /** Whether the modal is visible */
    open: boolean;
    /** Callback when the OK button is clicked */
    onOk: () => void;
    /** Modal title */
    title?: string;
    /** Callback when the modal is cancelled or closed */
    onCancel?: () => void;
    /** Text for the OK button */
    okText?: string;
    /** Loading state for the OK button */
    loading?: boolean;
}

export const UniversalModal = ({
    children,
    title,
    open,
    onOk,
    onCancel,
    okText,
    loading,
}: PropsWithChildren<IProps>) => {
    const { t } = useTranslation();

    return (
        <Modal
            title={title}
            open={open}
            onOk={onOk}
            onCancel={onCancel}
            centered
            width={550}
            okText={okText}
            cancelText={t("cancel")}
            loading={loading}
        >
            {children}
        </Modal>
    );
};
