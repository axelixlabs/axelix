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
import { Alert, Button } from "antd";
import dayjs from "dayjs";
import { useState } from "react";
import { useTranslation } from "react-i18next";

import { LicenseKeyForm, UniversalModal } from "@/components";
import { getLicenseDaysLeft, isLicenseExpiringSoon, shouldShowLicenseKeyAlert } from "@/helpers";
import { useAppSelector, useAuthority } from "@/hooks";
import { EAuthorities, ELicenseFormType } from "@/models";
import { LICENSE_ALERT_DISMISSED_AT_KEY } from "@/utils";

import styles from "./styles.module.css";

interface IProps {
    /**
     * TODO: Remove optional in the future
     */
    hideSider?: boolean;
}

export const LicenseStatusAlert = ({ hideSider }: IProps) => {
    const { t } = useTranslation();

    const enterLicenseAccess = useAuthority(EAuthorities.LICENSE_ENTER);

    const { licensing } = useAppSelector((state) => state.settings);

    const [licenseFormType, setLicenseFormType] = useState<ELicenseFormType | null>(null);
    const [shouldShow, setShouldShow] = useState<boolean>(shouldShowLicenseKeyAlert);

    const onClose = (): void => {
        setLicenseFormType(null);
    };

    const onDismiss = (): void => {
        localStorage.setItem(LICENSE_ALERT_DISMISSED_AT_KEY, String(Date.now()));
        setShouldShow(false);
    };

    if (!enterLicenseAccess || !shouldShow || !isLicenseExpiringSoon(licensing.validUntil)) {
        return null;
    }

    const msLeft = dayjs(licensing.validUntil).diff(dayjs());
    const daysLeft = getLicenseDaysLeft(msLeft);
    const expiryDate = dayjs(licensing.validUntil).format("YYYY-MM-DD");

    return (
        <>
            <div className={`${styles.AlertWrapper} ${!hideSider ? styles.AlertWithSider : ""}`}>
                <Alert
                    title={t("LicenseStatusAlert.licenseExpiringSoonAlertTitle", { count: daysLeft, date: expiryDate })}
                    type="warning"
                    action={
                        <Button type="link" size="small" onClick={() => setLicenseFormType(ELicenseFormType.UPDATE)}>
                            {t("LicenseStatusAlert.licenseAlertActionText")}
                        </Button>
                    }
                    className={styles.Alert}
                    closable={{ closeIcon: true, onClose: onDismiss }}
                />
            </div>

            <UniversalModal
                open={licenseFormType !== null}
                onOk={onClose}
                onClose={onClose}
                displayCancel={false}
                displayOkay={false}
            >
                {licenseFormType && (
                    <LicenseKeyForm
                        licenseFormType={licenseFormType}
                        setLicenseFormType={setLicenseFormType}
                        licensing={licensing}
                    />
                )}
            </UniversalModal>
        </>
    );
};
