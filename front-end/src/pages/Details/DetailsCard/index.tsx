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
import type { PropsWithChildren } from "react";
import { useTranslation } from "react-i18next";

import type { IDetailsCardRecord } from "models";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Details card title
     */
    title: string;
    /**
     * Prefix to be used when translating {@link records} via i18n.
     */
    i18nPropertiesPrefix: string;
    /**
     * Details card records
     */
    records: IDetailsCardRecord[];
}

export const DetailsCard = ({ children, i18nPropertiesPrefix, title, records }: PropsWithChildren<IProps>) => {
    const { t } = useTranslation();

    return (
        <div className={`CustomizedTable ${styles.Card}`}>
            <div className="TableHeader">
                <div className={`RowChunk ${styles.TableHeaderRowChunk}`}>
                    {children}
                    {t(title)}
                </div>
            </div>

            {records.map(({ key, value }) => (
                <div className="TableRow" key={key}>
                    <div className="RowChunk">{t(`${i18nPropertiesPrefix}.${key}`)}</div>
                    <div className={`RowChunk ${styles.ValueChunk}`}>
                        <div className={styles.ValueWrapper}>{value}</div>
                    </div>
                </div>
            ))}
        </div>
    );
};
