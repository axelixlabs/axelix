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
import { Tag } from "antd";
import LinkIcon from "assets/icons/link.svg?react";
import { Link, useParams } from "react-router-dom";

import { TooltipWithCopy } from "components";
import { defineBeanScopeColor, normalizeHtmlElementId } from "helpers";
import type { IBean } from "models";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Single bean
     */
    bean: IBean;
}

export const BeanAccordionLabels = ({ bean }: IProps) => {
    const { beanName, className, scope, aliases, isConfigPropsBean } = bean;
    const { instanceId } = useParams();

    return (
        <div
            // These data attributes are later needed for scrolling. See scrollToAccordionById() function
            data-bean-entry
            data-bean-name={beanName}
            data-bean-class={className}
            data-bean-aliases={aliases}
            className={styles.AccordionHeader}
        >
            <div className={styles.AccordionHeaderContent}>
                <div className={styles.BeanNameWrapper}>
                    <TooltipWithCopy text={beanName} />
                    {isConfigPropsBean && (
                        <Link
                            to={`/instance/${instanceId}/config-props#${normalizeHtmlElementId(beanName)}`}
                            onClick={(e) => e.stopPropagation()}
                        >
                            <LinkIcon />
                        </Link>
                    )}
                </div>
                <div className={styles.ClassName}>
                    <TooltipWithCopy text={className} />
                </div>
            </div>
            <Tag variant="outlined" color={defineBeanScopeColor(scope)} className={styles.Scope}>
                {scope}
            </Tag>
        </div>
    );
};
