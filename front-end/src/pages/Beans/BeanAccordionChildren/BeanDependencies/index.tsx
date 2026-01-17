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
import { useTranslation } from "react-i18next";

import { TooltipWithCopy } from "components";
import { ESearchSubject, type IDependency } from "models";
import { scrollToAccordionById } from "utils";

import styles from "./styles.module.css";

interface IProps {
    /**
     * List of dependencies
     */
    dependencies: IDependency[];
}

export const BeanDependencies = ({ dependencies }: IProps) => {
    const { t } = useTranslation();

    return (
        <>
            <div className={styles.AccordionBodyChunkTitle}>{t("Beans.dependencies")}:</div>
            <div>
                {dependencies.map(({ name }) => (
                    <div key={name} className={styles.AccordionBodyChunkList}>
                        <div className={styles.DependencyWrapper}>
                            <div
                                className={styles.Dependency}
                                onClick={() => scrollToAccordionById(name, ESearchSubject.BEAN_NAME_OR_ALIAS)}
                            >
                                <TooltipWithCopy text={name} />
                            </div>
                        </div>
                    </div>
                ))}
            </div>
        </>
    );
};
