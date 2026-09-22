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
import dayjs from "dayjs";
import { useTranslation } from "react-i18next";

import { type IFrameworkSupportWindow } from "@/models";
import { DEPENDENCY_DATE_FORMAT } from "@/utils";

import { FrameworkWindowPanel } from "./FrameworkWindowPanel";
import styles from "./styles.module.css";

interface IProps {
    /**
     * The maintenance window of the framework the instance runs on.
     */
    framework: IFrameworkSupportWindow;

    /**
     * The ISO timestamp the analysis was taken at.
     */
    analyzedAt: string;
}

export const FrameworkWindow = ({ framework, analyzedAt }: IProps) => {
    const { t } = useTranslation();

    return (
        <>
            <div className={styles.MainWrapper}>
                <div className={styles.Caption}>
                    <span className={`TextUltraSmall ${styles.CaptionLabel}`}>
                        {t("DependenciesAnalyzer.framework.caption", { framework: framework.name })}
                    </span>
                    <span className={`TextUltraSmall ${styles.CaptionSource}`}>
                        {t("DependenciesAnalyzer.framework.source", {
                            date: dayjs(analyzedAt).format(DEPENDENCY_DATE_FORMAT),
                        })}
                    </span>
                </div>

                <FrameworkWindowPanel framework={framework} />
            </div>
        </>
    );
};
