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
import { Badge } from "antd";

interface IProps {
    /**
     * Wallboard instance card data
     */
    data: IInstanceCard;
}

export const WallboardCard = ({ data }: IProps) => {
    return (
        <Link to={`/instance/${data.instanceId}/details`} className={styles.Card}>
            <div className={styles.CardHeader}>
                {data.name}
                <Badge
                    status="processing"
                    color="#00ab55"
                    styles={{
                        indicator: {
                            width: "8px",
                            height: "8px",
                        },
                    }}
                />
            </div>
            <div className={styles.CardBody}>
                <div className={styles.CardBodyChunk}>
                    <div className={styles.BodyChunkLabel}>Version:</div>
                    <div className={styles.BodyChunkValue}>{data.serviceVersion}</div>
                </div>

                <div className={styles.CardBodyChunk}>
                    <span className={styles.BodyChunkLabel}>Spring Boot</span>
                    <span className={styles.BodyChunkValue}>{data.springBootVersion}</span>
                </div>

                <div className={styles.CardBodyChunk}>
                    <span className={styles.BodyChunkLabel}>Java</span>
                    <span className={styles.BodyChunkValue}>{data.javaVersion}</span>
                </div>

                <div className={styles.CardFooter}>
                    <div className={styles.Commit}>Commit: {data.commitShaShort}</div>
                    <span className={styles.Time}>{data.deployedFor}</span>
                </div>
            </div>
        </Link>
    );
};
