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
import { type Dispatch, type SetStateAction, useState } from "react";

import { UniversalModal } from "@/components";
import { isEnterpriseLicense } from "@/helpers/license";
import { ELicenseFormType, type ILicensing } from "@/models";

import { EnterpriseLicenseDetails } from "./EnterpriseLicenseDetails";
import { EnterLicenseKeyForm } from "./LicenseKeyForm";
import { OSSLicenseDetails } from "./OSSLicenseDetails";

interface IProps {
    /**
     * Whether the modal is open
     */
    isModalOpen: boolean;

    /**
     * Setter for the modal open state
     */
    setIsModalOpen: Dispatch<SetStateAction<boolean>>;

    licensing: ILicensing;
}

export const LicenseModal = ({ isModalOpen, setIsModalOpen, licensing }: IProps) => {
    const [licenseFormType, setLicenseFormType] = useState<ELicenseFormType | null>(null);

    const onClose = (): void => {
        setIsModalOpen(false);
        setLicenseFormType(null);
    };

    const existingLicenseDetails = isEnterpriseLicense(licensing) ? (
        <EnterpriseLicenseDetails setLicenseFormType={setLicenseFormType} licensing={licensing} />
    ) : (
        <OSSLicenseDetails setLicenseFormType={setLicenseFormType} licensing={licensing} />
    );

    return (
        <>
            <UniversalModal
                open={isModalOpen}
                onOk={onClose}
                onClose={onClose}
                displayCancel={false}
                displayOkay={false}
            >
                {licenseFormType ? (
                    <EnterLicenseKeyForm
                        licenseFormType={licenseFormType}
                        setLicenseFormType={setLicenseFormType}
                        licensing={licensing}
                    />
                ) : (
                    existingLicenseDetails
                )}
            </UniversalModal>
        </>
    );
};
