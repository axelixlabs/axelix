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

import { UniversalModal } from "components";
import { useAppSelector } from "hooks";

import { EnterKeyForm } from "./EnterKeyForm";
import { LicenseDetails } from "./LicenseDetails";

interface IProps {
    /**
     * Whether the modal is open
     */
    open: boolean;

    /**
     * Setter for the modal open state
     */
    setOpen: Dispatch<SetStateAction<boolean>>;

    isEnterprise: boolean;
}

export const LicenseModal = ({ open, setOpen, isEnterprise }: IProps) => {
    const { licensing } = useAppSelector((state) => state.settings);

    const [isFormOpen, setIsFormOpen] = useState<boolean>(false);

    const onClose = (): void => {
        setOpen(false);
        setIsFormOpen(false);
    };

    return (
        <>
            <UniversalModal open={open} onOk={onClose} onClose={onClose} displayCancel={false} displayOkay={false}>
                {isFormOpen ? (
                    <EnterKeyForm setIsFormOpen={setIsFormOpen} licensing={licensing} />
                ) : (
                    <LicenseDetails isEnterprise={isEnterprise} setIsFormOpen={setIsFormOpen} licensing={licensing} />
                )}
            </UniversalModal>
        </>
    );
};
