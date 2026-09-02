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
import type { ChangeEvent, Dispatch, SetStateAction } from "react";
import { useTranslation } from "react-i18next";

import styles from "./styles.module.css";

interface IProps {
    setLicenseKey: Dispatch<SetStateAction<string>>;
}

export const UploadLicenseKeyFile = ({ setLicenseKey }: IProps) => {
    const { t } = useTranslation();
    const { message } = App.useApp();

    const handleFileUpload = (e: ChangeEvent<HTMLInputElement>): void => {
        const uploadedFile = e.target.files?.[0];

        if (!uploadedFile) {
            return;
        }

        const reader = new FileReader();

        reader.onload = (loadEvent) => {
            const fileContent = loadEvent.target?.result;

            if (typeof fileContent === "string") {
                const trimmedContent = fileContent.trim();
                setLicenseKey(trimmedContent);
            }
        };

        reader.onerror = () => {
            message.error(t("LicenseModal.Form.fileReadError"));
        };

        reader.readAsText(uploadedFile);

        e.target.value = "";
    };

    return (
        <>
            <div>
                <span className={styles.Or}>{t("LicenseModal.Form.or")}</span>

                <label className={styles.FileUploadLabel}>
                    {t("LicenseModal.Form.uploadFile")}
                    <input type="file" accept=".lic" onChange={handleFileUpload} className={styles.UploadInput} />
                </label>
            </div>
        </>
    );
};
