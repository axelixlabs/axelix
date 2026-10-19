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
import { Tooltip } from "antd";
import type { TooltipPlacement } from "antd/es/tooltip";
import type { PropsWithChildren, ReactNode } from "react";

import styles from "./styles.module.css";

interface IProps {
    /**
     * The rich content shown inside the tooltip.
     */
    content: ReactNode;

    /**
     * Tooltip position relative to the trigger.
     */
    placement?: TooltipPlacement;
}

export const HintTooltip = ({ children, content, placement = "top" }: PropsWithChildren<IProps>) => {
    return (
        <>
            <Tooltip
                title={<div className={`TextUltraSmall ${styles.Content}`}>{content}</div>}
                placement={placement}
                // TODO: Improve in the future
                color="#0f172a"
                styles={{ container: { padding: 0 } }}
            >
                {children}
            </Tooltip>
        </>
    );
};
