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
import { Checkbox } from "antd";
import { useTranslation } from "react-i18next";

import styles from "../styles.module.css";

interface IProps {
    /**
     * A string values tag. This 'tag' serves as the key in the i18n dictionary, which in turn represents the
     * short technical term, that describes what {@link value} flag really represents (i.e. {@link IBean.isLazyInit},
     * or {@link IBean.isPrimary} etc.)
     */
    valueTag: string;

    /**
     * The value of the boolean flag (on / off)
     */
    value: boolean;
}

export const BeanBooleanFlag = ({ value, valueTag }: IProps) => {
    const { t } = useTranslation();

    return (
        <>
            <div className={styles.AccordionBodyChunkTitle}>{t(`Beans.${valueTag}`)}:</div>
            <div>
                <Checkbox checked={value} className={styles.Flag} />
            </div>
        </>
    );
};
