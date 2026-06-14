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
import { ExclamationCircleFilled } from "@ant-design/icons";

import { App } from "antd";
import type { ReactNode } from "react";
import { useTranslation } from "react-i18next";

interface IConfirmableActionOptions {
    /**
     * Modal title.
     */
    title: ReactNode;

    /**
     * Modal content.
     */
    content?: ReactNode;

    /**
     * Callback executed after the confirmation.
     */
    onOk: () => void;

    /**
     * Optional text for the confirmation button.
     */
    okText?: string;

    /**
     * Optional text for the cancel button.
     */
    cancelText?: string;
}

export const useConfirmableAction = () => {
    const { modal } = App.useApp();
    const { t } = useTranslation();

    return ({ title, content, onOk, okText, cancelText }: IConfirmableActionOptions): void => {
        modal.confirm({
            icon: <ExclamationCircleFilled />,
            title: title,
            content: content,
            centered: true,
            okText: okText ?? t("confirm"),
            cancelText: cancelText ?? t("cancel"),
            onOk: onOk,
        });
    };
};
