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
import { Popover } from "antd";
import type { PropsWithChildren } from "react";

import { EnterpriseFeaturePopoverContent } from "./EnterpriseFeaturePopoverContent";
import styles from "./styles.module.css";

import { EnterpriseCrownIcon } from "assets";

// TODO: Fix in the future
const isEnterprise = false;

interface IProps {
    /**
     * URL for the documentation link
     */
    docsHref: string;

    /**
     * Additional styles for the children wrapper element
     */
    className?: string;
}

export const EnterpriseFeature = ({ children, docsHref, className }: PropsWithChildren<IProps>) => {
    if (isEnterprise) {
        return children;
    }

    return (
        <>
            <Popover trigger="click" content={<EnterpriseFeaturePopoverContent docsHref={docsHref} />}>
                <div className={`${styles.ChildrenWrapper} ${className ?? ""}`}>
                    <EnterpriseCrownIcon className={styles.EnterpriseCrownIcon} />
                    {children}
                </div>
            </Popover>
        </>
    );
};
