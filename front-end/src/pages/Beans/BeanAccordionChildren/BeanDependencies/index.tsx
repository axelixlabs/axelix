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
import { Link } from "react-router-dom";

import { TooltipWithCopy } from "components";
import { normalizeHtmlElementId } from "helpers";
import { ESearchSubject, type IDependency } from "models";
import { scrollToAccordionById } from "utils";

import styles from "./styles.module.css";

import LinkIcon from "assets/icons/link.svg";

interface IProps {
    /**
     * the id of the current application instance
     */
    instanceId: string;

    /**
     * List of dependencies
     */
    dependencies: IDependency[];
}

export const BeanDependencies = ({ dependencies, instanceId }: IProps) => {
    const { t } = useTranslation();

    return (
        <>
            <div className={styles.AccordionBodyChunkTitle}>{t("Beans.dependencies")}:</div>
            <div>
                {dependencies.map(({ name, isConfigPropsDependency }) => (
                    <div key={name} className={styles.AccordionBodyChunkList}>
                        <div className={styles.DependencyWrapper}>
                            <div
                                className={styles.Dependency}
                                onClick={() => scrollToAccordionById(name, ESearchSubject.BEAN_NAME_OR_ALIAS)}
                            >
                                <TooltipWithCopy text={name} />
                            </div>
                            {isConfigPropsDependency && (
                                <Link to={`/instance/${instanceId}/config-props#${normalizeHtmlElementId(name)}`}>
                                    <img src={LinkIcon} alt="Link icon" />
                                </Link>
                            )}
                        </div>
                    </div>
                ))}
            </div>
        </>
    );
};
