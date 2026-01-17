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
import { Link } from "react-router-dom";

import type { IInstanceCard } from "models";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Wallboard instance card data
     */
    data: IInstanceCard;
}

export const WallboardCard = ({ data }: IProps) => {
    return (
        <Link to={`/instance/${data.instanceId}/details`} className={`${styles.Card} ${styles[`Card${data.status}`]}`}>
            <div className={`${styles.CardHeader} ${styles[`CardHeader${data.status}`]}`}>{data.name}</div>
            <div className={styles.CardBody}>
                <div>Version: {data.serviceVersion}</div>
                <div>Spring Boot: {data.springBootVersion}</div>
                <div>Java: {data.javaVersion}</div>
                <div className={styles.HashAndTimeWrapper}>
                    <span>Commit: {data.commitShaShort}</span>
                    <span>{data.deployedFor}</span>
                </div>
            </div>
        </Link>
    );
};
