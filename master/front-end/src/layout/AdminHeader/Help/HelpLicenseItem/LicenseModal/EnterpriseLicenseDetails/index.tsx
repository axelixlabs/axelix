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
import type { Dispatch, SetStateAction } from "react";
import { useTranslation } from "react-i18next";

import { getTimeLeftText } from "@/helpers";
import { ELicenseFormType, type ILicensing } from "@/models";

import { LicenseBadge } from "../../LicenseBadge";
import { LicenseFunctions } from "../LicenseFunctions";
import { LicenseRecord } from "../LicenseRecord";

import styles from "./styles.module.css";

interface IProps {
    licensing: ILicensing;
    setLicenseFormType: Dispatch<SetStateAction<ELicenseFormType | null>>;
}

export const EnterpriseLicenseDetails = ({ licensing, setLicenseFormType }: IProps) => {
    const { t } = useTranslation();

    const { functions, validUntil } = licensing;

    const timeLeft = getTimeLeftText(validUntil, t);

    return (
        <>
            <div className="TextLarge">{t("license")}</div>

            <div className={styles.ContentWrapper}>
                <div className={`TextSmall ${styles.Meta}`}>
                    <LicenseBadge isEnterprise enterpriseText="Enterprise" ossText="Open Source" />

                    <div className={`${styles.MetaStatus} ${styles.MetaStatusEnterprise}`}>
                        <span>{t("LicenseModal.active")}</span>
                        <span>•</span>
                        <span>{timeLeft}</span>
                    </div>
                </div>

                <LicenseRecord licensing={licensing} />

                <LicenseFunctions functions={functions} />

                <div className={styles.ActionsWrapper}>
                    <Button type="primary" onClick={() => setLicenseFormType(ELicenseFormType.UPDATE)}>
                        {t("LicenseModal.updateKey")}
                    </Button>
                </div>
            </div>
        </>
    );
};
