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
import { useParams } from "react-router-dom";

import { type IBean } from "models";

import { BeanBooleanFlag } from "./BeanBooleanFlag";
import { BeanDependencies } from "./BeanDependencies";
import { BeanProxyType } from "./BeanProxyType";
import { BeanSimpleList } from "./BeanSimpleList";
import { BeanSource } from "./BeanSource";
import styles from "./styles.module.css";

interface IProps {
    /**
     * Single bean
     */
    bean: IBean;
}

export const BeanAccordionChildren = ({ bean }: IProps) => {
    const { instanceId } = useParams();

    return (
        <div className={styles.AccordionBody}>
            {bean.dependencies.length > 0 && (
                <BeanDependencies instanceId={instanceId!} dependencies={bean.dependencies}></BeanDependencies>
            )}
            {bean.aliases.length > 0 && <BeanSimpleList valuesTag="aliases" values={bean.aliases}></BeanSimpleList>}
            {bean.qualifiers.length > 0 && (
                <BeanSimpleList valuesTag="qualifiers" values={bean.qualifiers}></BeanSimpleList>
            )}
            <BeanProxyType proxyType={bean.proxyType} />
            <BeanBooleanFlag valueTag={"isLazyInitBean"} value={bean.isLazyInit} />
            <BeanBooleanFlag valueTag={"isPrimaryBean"} value={bean.isPrimary} />
            <BeanSource bean={bean} />
        </div>
    );
};
