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
import { App } from "antd";
import { type MouseEvent } from "react";
import { useTranslation } from "react-i18next";

import styles from "./styles.module.css";

import { CopyIcon } from "assets";

interface IProps {
    /**
     * Text that will be copied
     */
    text: string;
}

export const Copy = ({ text }: IProps) => {
    const { t } = useTranslation();
    const { message } = App.useApp();

    const handleCopy = async (e: MouseEvent<SVGSVGElement>): Promise<void> => {
        e.stopPropagation();

        try {
            await navigator.clipboard.writeText(text);
            message.success(t("copied"));
        } catch {
            message.error(t("copyFailed"));
        }
    };

    return <CopyIcon onClick={handleCopy} className={styles.CopyIcon} />;
};
