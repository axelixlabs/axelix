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
import { Alert } from "antd";
import dayjs from "dayjs";
import { Trans, useTranslation } from "react-i18next";

import type { IAlertConfigItem, TLicenseCheckResponseBody } from "models";
import { ISO_DATE_FORMAT, LICENSE_KEY_VALID_FLAG } from "utils";

import styles from "./styles.module.css";

interface IProps {
    validationData: TLicenseCheckResponseBody | null;
}

export const LicenseKeyFormAlert = ({ validationData }: IProps) => {
    const { t } = useTranslation();

    const getAlertKey = () => {
        if (validationData) {
            if ("status" in validationData && validationData?.status === LICENSE_KEY_VALID_FLAG) {
                return LICENSE_KEY_VALID_FLAG;
            }

            if ("errorCode" in validationData && validationData.errorCode) {
                return validationData.errorCode;
            }
        }
    };

    const alertKey = getAlertKey();

    if (!validationData || !alertKey) {
        return null;
    }

    const getIssuedTo = (): string => {
        if (validationData && "issuedTo" in validationData) {
            return validationData.issuedTo;
        }

        return validationData.attributes?.issuedTo;
    };

    const getExpiredDate = (): string => {
        if (!validationData || !("errorCode" in validationData)) {
            return "-";
        }

        return dayjs(validationData.attributes.expiredAt).format(ISO_DATE_FORMAT);
    };

    const getValidUntilDate = (): string => {
        if (!validationData || !("status" in validationData)) {
            return "-";
        }

        return dayjs(validationData.validUntil).format(ISO_DATE_FORMAT);
    };

    const alertConfigs: Record<string, IAlertConfigItem> = {
        VALID: {
            type: "success",
            title: t("LicenseModal.Form.Alert.Valid.title"),
            description: (
                <Trans
                    t={t}
                    i18nKey="LicenseModal.Form.Alert.Valid.description"
                    values={{ issuedTo: getIssuedTo() ?? "-", validUntil: getValidUntilDate() }}
                    components={[<b key="0" />]}
                />
            ),
        },
        LICENSE_ALREADY_ACTIVE: {
            type: "error",
            title: t("LicenseModal.Form.Alert.AlreadyActive.title"),
            description: t("LicenseModal.Form.Alert.AlreadyActive.description"),
        },
        LICENSE_EXPIRED: {
            type: "error",
            title: t("LicenseModal.Form.Alert.Expired.title", { date: getExpiredDate() }),
            description: (
                <Trans
                    t={t}
                    i18nKey="LicenseModal.Form.Alert.Expired.description"
                    values={{ issuedTo: getIssuedTo() ?? "-" }}

                    // TODO: Add href from .env
                    components={[
                        <a key="0" href="#" target="_blank" rel="noopener noreferrer" className={styles.ContactLink} />,
                    ]}
                />
            ),
        },
        LICENSE_SIGNATURE_INVALID: {
            type: "error",
            title: t("LicenseModal.Form.Alert.SignatureInvalid.title"),
            description: t("LicenseModal.Form.Alert.SignatureInvalid.description"),
        },
    };

    const alertConfigData = alertConfigs[alertKey];

    if (!alertConfigData) {
        return null;
    }

    // TODO: Improve this part in the future
    const contentColor = alertConfigData.type === "success" ? "#00ab55" : "#ff000a";

    return (
        <Alert
            title={alertConfigData.title}
            description={<div className="TextSmall">{alertConfigData.description}</div>}
            type={alertConfigData.type}
            classNames={{
                root: styles.AlertRoot,
            }}
            styles={{
                root: {
                    padding: "16px",
                },
                title: {
                    fontSize: "inherit",
                    fontWeight: 600,
                    color: contentColor,
                },
                description: {
                    color: contentColor,
                },
            }}
        />
    );
};
