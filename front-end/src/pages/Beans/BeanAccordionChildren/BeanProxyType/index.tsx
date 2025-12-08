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

import { resolveProxying } from "helpers/beans";
import { EProxyType } from "models";

import styles from "../styles.module.css";

interface IProps {
    /**
     * The proxying algorithm used to create the instance of the bean. Might be null
     * in case the backend was unable to figure it out.
     */
    proxyType: EProxyType | null;
}

export const BeanProxyType = ({ proxyType }: IProps) => {
    const { t } = useTranslation();

    return (
        <>
            <div className={styles.AccordionBodyChunkTitle}>{t("Beans.beanProxyType")}:</div>
            <div>{resolveProxying(t, proxyType)}</div>
        </>
    );
};
