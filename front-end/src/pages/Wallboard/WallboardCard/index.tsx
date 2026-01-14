/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
