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
import type { Dispatch, SetStateAction } from "react";
import { Trans, useTranslation } from "react-i18next";

import { getTimeLeftText, isEnterpriseLicense } from "helpers/license";
import { ELicenseFormType, type ILicensing } from "models";
import { LGPL_LINK } from "utils";

import { LicenseBadge } from "../../LicenseBadge";
import { LicenseFunctions } from "../LicenseFunctions";
import { LicenseRecord } from "../LicenseRecord";

import { LicenseDetailsFooter } from "./LicenseDetailsFooter";
import styles from "./styles.module.css";

interface IProps {
    licensing: ILicensing;
    setLicenseFormType: Dispatch<SetStateAction<ELicenseFormType | null>>;
}

export const LicenseDetails = ({ setLicenseFormType, licensing }: IProps) => {
    const { t } = useTranslation();

    const { license, functions, validUntil } = licensing;

    const isEnterprise = isEnterpriseLicense(licensing);
    const timeLeft = getTimeLeftText(validUntil, t);

    return (
        <>
            <div className="TextLarge">{t("license")}</div>

            <div className={styles.ContentWrapper}>
                <div className={`TextSmall ${styles.Meta}`}>
                    <LicenseBadge isEnterprise={isEnterprise} enterpriseText="Enterprise" ossText="Open Source" />

                    <div
                        className={`${styles.MetaStatus} ${isEnterprise ? styles.MetaStatusEnterprise : styles.MetaStatusOSS}`}
                    >
                        <span>{isEnterprise ? t("LicenseModal.active") : license}</span>
                        <span>•</span>
                        <span>{isEnterprise ? timeLeft : t("LicenseModal.noExpiry")}</span>
                    </div>
                </div>

                {isEnterprise ? (
                    <LicenseRecord licensing={licensing} />
                ) : (
                    <div>
                        <Trans
                            t={t}
                            i18nKey="LicenseModal.ossDescription"
                            values={{ license: license }}
                            components={{
                                b: <b />,
                                a: (
                                    <a
                                        href={LGPL_LINK}
                                        target="_blank"
                                        rel="noopener noreferrer"
                                        className={styles.LicenseLink}
                                    />
                                ),
                            }}
                        />
                    </div>
                )}

                <LicenseFunctions functions={functions} />

                <LicenseDetailsFooter isEnterprise={isEnterprise} setLicenseFormType={setLicenseFormType} />
            </div>
        </>
    );
};
