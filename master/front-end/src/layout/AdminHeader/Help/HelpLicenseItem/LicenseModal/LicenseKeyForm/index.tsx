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
import { Input, Spin } from "antd";
import { type Dispatch, type SetStateAction, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";

import { isEnterpriseLicense } from "@/helpers";
import { ELicenseFormType, type ILicensing, type TLicenseCheckResponseBody } from "@/models";
import { checkLicenseKey } from "@/services";
import { LICENSE_KEY_VALID_FLAG } from "@/utils";

import { LicenseKeyFormAlert } from "./LicenseKeyFormAlert";
import { LicenseKeyFormFooter } from "./LicenseKeyFormFooter";
import { LicenseKeyInfo } from "./LicenseKeyInfo";
import styles from "./styles.module.css";

const { TextArea } = Input;

interface IProps {
    licensing: ILicensing;
    licenseFormType: ELicenseFormType;
    setLicenseFormType: Dispatch<SetStateAction<ELicenseFormType | null>>;
}

export const EnterLicenseKeyForm = ({ licenseFormType, setLicenseFormType, licensing }: IProps) => {
    const { t } = useTranslation();

    const isEnterprise = isEnterpriseLicense(licensing);

    const [licenseKey, setLicenseKey] = useState<string>("");
    const [checkResponse, setCheckResponse] = useState<TLicenseCheckResponseBody | null>(null);
    const [isChecking, setIsChecking] = useState<boolean>(false);

    const isValidLicenseKey =
        checkResponse && "status" in checkResponse ? checkResponse.status === LICENSE_KEY_VALID_FLAG : null;

    const validateLicenseKey = (cancelled: boolean): void => {
        checkLicenseKey(licenseKey)
            .then(({ data }) => {
                if (!cancelled) {
                    setCheckResponse(data);
                }
            })
            .catch((e) => {
                if (!cancelled) {
                    setCheckResponse(e.response?.data ?? null);
                }
            })
            .finally(() => {
                if (!cancelled) {
                    setIsChecking(false);
                }
            });
    };

    useEffect(() => {
        if (!licenseKey) {
            setCheckResponse(null);
            setIsChecking(false);
            return;
        }

        setCheckResponse(null);
        setIsChecking(true);

        let cancelled = false;

        const timeoutId = setTimeout(() => {
            validateLicenseKey(cancelled);
        }, 500);

        return () => {
            cancelled = true;
            clearTimeout(timeoutId);
        };
    }, [licenseKey]);

    const textareaStatus = isValidLicenseKey === null ? undefined : isValidLicenseKey ? "success" : "error";

    return (
        <>
            <div className={`TextSmall ${styles.EnterKeyFormTitle}`}>{t("license")}</div>

            <div className={styles.EnterKeyFormWrapper}>
                <div className="TextLarge">
                    {licenseFormType === ELicenseFormType.CREATE
                        ? t("LicenseModal.Form.enterKeyTitle")
                        : t("LicenseModal.Form.updateKeyTitle")}
                </div>
                <p className={`TextSmall ${styles.EnterKeyDescription}`}>
                    {t("LicenseModal.Form.enterKeyDescription")}
                </p>

                {isEnterprise && <LicenseKeyInfo licensing={licensing} />}

                <div className={styles.TextAreaWrapper}>
                    <TextArea
                        status={textareaStatus}
                        rows={6}
                        style={{ resize: "none", paddingRight: "30px" }}
                        value={licenseKey}
                        onChange={(e) => setLicenseKey(e.target.value)}
                    />

                    {/* TODO: Replace with our <Loader /> component in the future */}
                    {isChecking && <Spin size="small" className={styles.CheckingSpinner} />}
                </div>

                <LicenseKeyFormAlert validationData={checkResponse} />

                <LicenseKeyFormFooter
                    licenseKey={licenseKey}
                    setLicenseKey={setLicenseKey}
                    setLicenseFormType={setLicenseFormType}
                    isEnterprise={isEnterprise}
                    isValidLicenseKey={isValidLicenseKey}
                />
            </div>
        </>
    );
};
