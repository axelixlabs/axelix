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
import { Badge } from "antd";
import QuestionIcon from "assets/icons/question.svg?react";
import { Link } from "react-router-dom";

import { EInstanceStatus, type IInstanceCard } from "models";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Wallboard instance card data
     */
    data: IInstanceCard;
}

export const WallboardCard = ({ data }: IProps) => {
    const isUp = data.status === EInstanceStatus.UP;
    const isUnknown = data.status === EInstanceStatus.UNKNOWN;
    const isDown = data.status === EInstanceStatus.DOWN;

    return (
        <Link
            to={`/instance/${data.instanceId}/details`}
            className={`${styles.Card} ${!isUp ? styles[data.status] : ""}`}
        >
            <div className={styles.Header}>
                {/* TODO: Here we need to show an ellipsis with a tooltip */}
                <div className={styles.Title}>{data.name}</div>
                {!isUnknown ? (
                    <Badge
                        status={isDown ? "processing" : undefined}
                        color={isDown ? "#ff000a" : "#00ab55"}
                        styles={{
                            indicator: {
                                width: "8px",
                                height: "8px",
                            },
                        }}
                    />
                ) : (
                    <QuestionIcon className={styles.QuestionIcon} style={{ flexShrink: "0" }} />
                )}
            </div>
            <div className={styles.Body}>
                <div className={styles.BodyChunk}>
                    <div className={styles.Label}>Version</div>
                    <div className={styles.Value}>{data.serviceVersion}</div>
                </div>

                <div className={styles.BodyChunk}>
                    <div className={styles.Label}>Spring Boot</div>
                    <div className={styles.Value}>{data.springBootVersion}</div>
                </div>

                <div className={styles.BodyChunk}>
                    <div className={styles.Label}>Java</div>
                    <div className={styles.Value}>{data.javaVersion}</div>
                </div>

                <div className={styles.Footer}>
                    <div className={styles.Label}>Commit: {data.commitShaShort}</div>
                    <div className={styles.Value}>{data.deployedFor}</div>
                </div>
            </div>
        </Link>
    );
};
