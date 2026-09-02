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

export const LicenseKeyInfo = ({ licensing }: IProps) => {
    const { t } = useTranslation();

    const { licenseId, issuedTo, validUntil } = licensing;

    const validTo = validUntil && dayjs(validUntil).format(ISO_DATE_FORMAT);

    return (
        <>
            <div className={styles.ActiveLicenseKeyInfoWrapper}>
                <div className={styles.ActiveLicenseKeyInfoLabel}>{t("LicenseModal.Form.KeyInfo.currentlyActive")}</div>
                <div className={styles.ActiveLicenseKeyInfoValue}>{licenseId || "-"}</div>
                <div className={styles.ActiveLicenseKeyInfoOrg}>{issuedTo || "-"}</div>
                <div className={styles.ActiveLicenseKeyInfoValue}>
                    {validTo ? t("LicenseModal.Form.KeyInfo.validUntil", { date: validTo }) : "-"}
                </div>
            </div>
        </>
    );
};
