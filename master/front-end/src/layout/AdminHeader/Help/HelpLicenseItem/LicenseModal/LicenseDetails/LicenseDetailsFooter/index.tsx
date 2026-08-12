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

import { ELicenseFormType } from "models";

import styles from "./styles.module.css";

interface IProps {
    isEnterprise: boolean;
    setLicenseFormType: Dispatch<SetStateAction<ELicenseFormType | null>>;
}

export const LicenseDetailsFooter = ({ isEnterprise, setLicenseFormType }: IProps) => {
    const { t } = useTranslation();

    return (
        <>
            <div className={styles.ActionsWrapper}>
                {isEnterprise ? (
                    <Button type="primary" onClick={() => setLicenseFormType(ELicenseFormType.UPDATE)}>
                        {t("LicenseModal.updateKey")}
                    </Button>
                ) : (
                    <Button type="primary" onClick={() => setLicenseFormType(ELicenseFormType.CREATE)}>
                        {t("LicenseModal.enterKey")}
                    </Button>
                )}

                {/* <Button>{t("LicenseModal.thirdPartyNotices")}</Button> */}
            </div>
        </>
    );
};
