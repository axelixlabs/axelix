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
import { App, Button } from "antd";
import { type Dispatch, type SetStateAction, useState } from "react";
import { useTranslation } from "react-i18next";

import { useAppDispatch } from "@/hooks";
import type { ELicenseFormType } from "@/models";
import { enterLicenseKey, getAxelixSettings } from "@/services";
import { setAxelixSettings } from "@/store/slices";

import { UploadLicenseKeyFile } from "./UploadLicenseKeyFile";
import styles from "./styles.module.css";

interface IProps {
    setLicenseKey: Dispatch<SetStateAction<string>>;
    setLicenseFormType: Dispatch<SetStateAction<ELicenseFormType | null>>;
    isEnterprise: boolean;
    isValidLicenseKey: boolean | null;
    licenseKey: string;
}

export const LicenseKeyFormFooter = ({
    licenseKey,
    setLicenseKey,
    setLicenseFormType,
    isEnterprise,
    isValidLicenseKey,
}: IProps) => {
    const { t } = useTranslation();
    const [loading, setLoading] = useState(false);

    const dispatch = useAppDispatch();
    const { message } = App.useApp();

    const clickHandler = () => {
        setLoading(true);

        enterLicenseKey(licenseKey)
            .then(() => {
                getAxelixSettings()
                    .then((response) => {
                        dispatch(setAxelixSettings(response.data));
                        message.success(
                            isEnterprise ? t("LicenseModal.Form.keyUpdated") : t("LicenseModal.Form.keyActivated"),
                        );
                        setLicenseFormType(null);
                    })
                    .catch((reason) => {
                        // TODO: Insert an image of something went wrong
                        return reason;
                    })
                    .finally(() => {
                        setLoading(false);
                    });
            })
            .catch(() => {
                setLoading(false);
            });
    };

    return (
        <>
            <div className={styles.FormFooter}>
                <UploadLicenseKeyFile setLicenseKey={setLicenseKey} />

                <div className={styles.FormActionsWrapper}>
                    <Button onClick={() => setLicenseFormType(null)}>{t("cancel")}</Button>

                    <Button type="primary" onClick={clickHandler} disabled={!isValidLicenseKey} loading={loading}>
                        {isEnterprise ? t("LicenseModal.Form.replaceLicense") : t("LicenseModal.Form.activate")}
                    </Button>
                </div>
            </div>
        </>
    );
};
