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
import { LicenseBadge } from "layout/AdminHeader/Help/LicenseBadge";
import { useTranslation } from "react-i18next";

import { LicenseCapabilities } from "../LicenseIncludesCapabilities";
import { LicenseRecord } from "../LicenseRecord";

import styles from "./styles.module.css";
import type { ILicensing } from "models";

interface IProps {
    licensing: ILicensing
    isEnterprise: boolean;
    setIsFormOpen: any;
}

export const LicenseDetails = ({ isEnterprise, setIsFormOpen, licensing }: IProps) => {
    const { t } = useTranslation();

    const { license, functions } = licensing

    return (
        <>
            <div className="TextLarge">{t("license")}</div>

            <div className={styles.ContentWrapper}>
                <div className={`TextSmall ${styles.Meta}`}>
                    <LicenseBadge isEnterprise={isEnterprise} enterpriseText="Enterprise" ossText="Open Source" />

                    <div className={styles.MetaStatus}>
                        <span>{isEnterprise ? t("LicenseModal.enterpriseStatus") : license}</span>
                        <span>•</span>
                        <span>
                            {isEnterprise ? t("LicenseModal.expiresIn", { days: 152 }) : t("LicenseModal.noExpiry")}
                        </span>
                    </div>
                </div>

                {isEnterprise ? (
                    <LicenseRecord licensing={licensing} />
                ) : (
                    <p>{t("LicenseModal.ossDescription", { license: license })}</p>
                )}

                <LicenseCapabilities capabilities={functions} />

                <div className={styles.ActionsWrapper}>
                    <Button type="primary" onClick={() => setIsFormOpen(true)}>
                        {isEnterprise ? t("LicenseModal.updateKey") : t("LicenseModal.enterKey")}
                    </Button>

                    <Button>{t("LicenseModal.thirdPartyNotices")}</Button>
                </div>
            </div>
        </>
    );
};
