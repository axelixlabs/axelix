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
import { IHeaderMenu } from "@/models";

import { getLinkTargetProps } from "../../../utils";

import styles from "./styles.module.css";

interface IProps {
    menu: IHeaderMenu;
    onLinkClick: () => void;
}

export const CompactMenu = ({ menu: { columns }, onLinkClick }: IProps) => {
    return (
        <div className={styles.MainWrapper}>
            {columns
                .flatMap(({ links }) => links)
                .map(({ title, description, href, external }) => (
                    <a
                        key={title}
                        href={href}
                        className={styles.Link}
                        onClick={onLinkClick}
                        {...getLinkTargetProps(external)}
                    >
                        <span className={styles.LinkTitle}>{title}</span>
                        <span className={styles.LinkDescription}>{description}</span>
                    </a>
                ))}
        </div>
    );
};
