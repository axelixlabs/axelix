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
import { Button, Input } from "antd";
import { useState } from "react";
import { useTranslation } from "react-i18next";

import styles from "./styles.module.css";
import type { ILicensing } from "models";
import dayjs from "dayjs";

const { TextArea } = Input;

interface IProps {
    licensing: ILicensing
    setIsFormOpen: any;
}

export const EnterKeyForm = ({ setIsFormOpen, licensing }: IProps) => {
    const { t } = useTranslation();
    const [licenseKey, setLicenseKey] = useState<string>("");

    const { licenseId, issuedTo, validUntil } = licensing

    const validTo = validUntil && dayjs(validUntil).format("YYYY-MM-DD")

    return (
        <>
            <div className={`TextSmall ${styles.EnterKeyFormTitle}`}>{t("license")}</div>

            <div className={styles.EnterKeyFormWrapper}>
                <div className="TextLarge">
                    {true ? t("LicenseModal.Form.enterKeyTitle") : t("LicenseModal.Form.updateKeyTitle")}
                </div>

                <p className={`TextSmall ${styles.EnterKeyDescription}`}>
                    {t("LicenseModal.Form.enterKeyDescription")}
                </p>

                <div className={styles.ActiveLicenseKeyInfoWrapper}>
                    <div className={styles.ActiveLicenseKeyInfoLabel}>Currently active</div>
                    <div className={styles.ActiveLicenseKeyInfoValue}>{licenseId || "-"}</div>
                    <div className={styles.ActiveLicenseKeyInfoOrg}>{issuedTo || "-"}</div>
                    <div className={styles.ActiveLicenseKeyInfoValue}>{validTo ? `until ${validTo}` : "-"}</div>
                </div>

                <TextArea
                    rows={6}
                    style={{ resize: "none" }}
                    value={licenseKey}
                    onChange={(e) => setLicenseKey(e.target.value)}
                />

                <div className={styles.AlertWrapper}>
                    <div className={styles.AlertTitle}>
                        {t("LicenseModal.Form.keyExpiredTitle", { date: "2026-03-01" })}
                    </div>
                    <div className="TextSmall">
                        {t("LicenseModal.Form.keyExpiredDescription", { organization: "Contoso Financial AG" })}
                    </div>
                </div>

                <div className={styles.FormActionsWrapper}>
                    <Button onClick={() => setIsFormOpen(false)}>{t("cancel")}</Button>

                    <Button type="primary" onClick={() => setIsFormOpen(true)}>
                        {t("LicenseModal.Form.activate")}
                    </Button>
                </div>
            </div>
        </>
    );
};
