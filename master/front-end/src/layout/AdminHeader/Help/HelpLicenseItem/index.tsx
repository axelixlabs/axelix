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
import { useState } from "react";
import { useTranslation } from "react-i18next";

import { isEnterpriseLicense } from "@/helpers";
import { useAppSelector } from "@/hooks";

import { LicenseBadge } from "./LicenseBadge";
import { LicenseModal } from "./LicenseModal";
import styles from "./styles.module.css";

export const HelpLicenseItem = () => {
    const { t } = useTranslation();

    const { licensing } = useAppSelector((state) => state.settings);
    const [isLicenseModalOpen, setIsLicenseModalOpen] = useState<boolean>(false);

    return (
        <>
            <div className={styles.LicenseItemWrapper} onClick={() => setIsLicenseModalOpen(true)}>
                {t("license")}
                <LicenseBadge isEnterprise={isEnterpriseLicense(licensing)} enterpriseText="ENT" ossText="OSS" />
            </div>

            <LicenseModal
                isModalOpen={isLicenseModalOpen}
                setIsModalOpen={setIsLicenseModalOpen}
                licensing={licensing}
            />
        </>
    );
};
