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
import dayjs from "dayjs";
import { useTranslation } from "react-i18next";

import type { ILicensing } from "@/models";
import { ISO_DATE_FORMAT } from "@/utils";

import styles from "./styles.module.css";

interface IProps {
    licensing: ILicensing;
}

export const LicenseRecord = ({ licensing }: IProps) => {
    const { t } = useTranslation();

    const { issuedTo, licenseId, issuedAt, validUntil } = licensing;

    const validFrom = issuedAt && dayjs(issuedAt).format(ISO_DATE_FORMAT);
    const validTo = validUntil && dayjs(validUntil).format(ISO_DATE_FORMAT);

    return (
        <>
            <div className={styles.MainWrapper}>
                <div className={styles.RecordChunk}>
                    <div className={`TextUltraSmall ${styles.RecordLabel}`}>{t("LicenseModal.licensedTo")}</div>
                    <div>{issuedTo || "-"}</div>
                </div>
                <div className={styles.RecordChunk}>
                    <div className={`TextUltraSmall ${styles.RecordLabel}`}>{t("LicenseModal.licensedId")}</div>
                    <div>{licenseId || "-"}</div>
                </div>
                <div className={styles.RecordChunk}>
                    <div className={`TextUltraSmall ${styles.RecordLabel}`}>{t("LicenseModal.validFrom")}</div>
                    <div>{validFrom || "-"}</div>
                </div>
                <div className={styles.RecordChunk}>
                    <div className={`TextUltraSmall ${styles.RecordLabel}`}>{t("LicenseModal.validTo")}</div>
                    <div>{validTo || "-"}</div>
                </div>
            </div>
        </>
    );
};
