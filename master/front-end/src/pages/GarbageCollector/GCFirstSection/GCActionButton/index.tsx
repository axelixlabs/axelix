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
import type { HTMLAttributes, ReactNode } from "react";

import { useAuthority } from "hooks";
import { EAuthorities } from "models";

import styles from "../styles.module.css";

interface IProps extends HTMLAttributes<HTMLSpanElement> {
    /**
     * Indicates whether the button is in a loading state
     */
    loading: boolean;

    /**
     *  Callback invoked when the button is clicked
     */
    clickHandler: () => void;

    /**
     * Button's icon
     */
    icon: ReactNode;

    /**
     * Enables Ant Design danger styling
     */
    danger?: boolean;
}

export const GCActionButton = ({ loading, clickHandler, icon, danger = false, ...props }: IProps) => {
    const gcAccess = useAuthority(EAuthorities.GARBAGE_COLLECTOR);

    return (
        <>
            <span {...props}>
                <Button
                    icon={icon}
                    type="primary"
                    loading={loading}
                    onClick={clickHandler}
                    danger={danger}
                    className={styles.ActionButton}
                    disabled={!gcAccess}
                />
            </span>
        </>
    );
};
