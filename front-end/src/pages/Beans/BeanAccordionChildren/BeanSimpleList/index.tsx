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
import { useTranslation } from "react-i18next";

import styles from "../styles.module.css";

interface IProps {
    /**
     * A string values tag. This 'tag' serves as the key in the i18n dictionary, which in turn represents the
     * short technical term, that describes what {@link values} really are (i.e. they are qualifiers, aliases etc.)
     */
    valuesTag: string;

    /**
     * An array of values to be displayed.
     */
    values: string[];
}

/**
 * Functional component that represents a list of simple, non-clickable values to be displayed in the
 * bean collapse drop-down.
 */
export const BeanSimpleList = ({ valuesTag, values }: IProps) => {
    const { t } = useTranslation();

    return (
        <>
            <div className={styles.AccordionBodyChunkTitle}>{t(`Beans.${valuesTag}`)}:</div>

            <div>
                {!values.length ? (
                    <span>-</span>
                ) : (
                    values.map((values) => (
                        <div key={values} className={styles.AccordionBodyChunkList}>
                            <div className={styles.SimpleListValue}>{values}</div>
                        </div>
                    ))
                )}
            </div>
        </>
    );
};
