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
import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";

import styles from "./styles.module.css";
import type { ILicensing } from "models";
import { checkLicense, sendLicense } from "services";
import { LicenseKeyInfo } from "./LicenseKeyInfo";
import { UploadLicenseKeyFile } from "./UploadLicenseKeyFile";

const { TextArea } = Input;

interface IProps {
    licensing: ILicensing
    setIsFormOpen: any;
}

export const EnterKeyForm = ({ setIsFormOpen, licensing }: IProps) => {
    const { t } = useTranslation();
    const [licenseKey, setLicenseKey] = useState<string>("");
    const [isValidLicenseKey, setIsValidLicenseKey] = useState<boolean>(false);

    const { issuedTo } = licensing

    useEffect(() => {
        if (!licenseKey) {
            return;
        }

        checkLicense(licenseKey).then(() => {
            setIsValidLicenseKey(true);
        }).catch(() => {
            setIsValidLicenseKey(false);
        });
    }, [licenseKey])

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

                <LicenseKeyInfo licensing={licensing} />

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
                        {t("LicenseModal.Form.keyExpiredDescription", { organization: issuedTo || "-" })}
                    </div>
                </div>

                <div className={styles.FormFooter}>
                    <UploadLicenseKeyFile setLicenseKey={setLicenseKey} />

                    <div className={styles.FormActionsWrapper}>
                        <Button onClick={() => setIsFormOpen(false)}>{t("cancel")}</Button>

                        <Button type="primary" onClick={() => sendLicense(licenseKey)}>
                            {t("LicenseModal.Form.activate")}
                        </Button>
                    </div>
                </div>
            </div>
        </>
    );
};
