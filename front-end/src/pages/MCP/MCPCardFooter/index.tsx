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
import { Popover, Tag } from "antd";

import type { IMCPAnnotation } from "models";

import styles from "./styles.module.css";

import { LinesIcon } from "assets";

interface IProps {
    /**
     * MCP tool annotations
     */
    annotations: IMCPAnnotation;
}

export const MCPCardFooter = ({ annotations }: IProps) => {
    const { readOnlyHint, idempotentHint, destructiveHint, openWorldHint } = annotations;

    const annotationItems = [
        { label: "Read-only", value: readOnlyHint },
        { label: "Idempotent", value: idempotentHint },
        { label: "Destructive", value: destructiveHint },
        { label: "Open-world", value: openWorldHint },
    ];

    const getTagClass = (value: boolean): string => {
        return value ? styles.GreenTag : styles.OrangeTag;
    };

    return (
        <div className={styles.Footer}>
            <Tag className={getTagClass(readOnlyHint)}>Read-only</Tag>
            <Tag className={getTagClass(idempotentHint)}>Idempotent</Tag>

            <Popover
                title="Annotations"
                trigger="click"
                content={
                    <div className={styles.AnnotationWrapper}>
                        {annotationItems.map(({ label, value }) => (
                            <>
                                <div>{label}</div>
                                <div>
                                    <Tag className={`${getTagClass(value)} ${styles.AnnotationTag}`}>
                                        {String(value)}
                                    </Tag>
                                </div>
                            </>
                        ))}
                    </div>
                }
            >
                <LinesIcon className={styles.LinesIcon} />
            </Popover>
        </div>
    );
};
