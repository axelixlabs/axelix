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
import { Trans, useTranslation } from "react-i18next";

import styles from "./styles.module.css";

interface IProps {
    applicationsTotal: number;
    footerRight?: string;
}

export const NoKotlinBanner = ({ applicationsTotal, footerRight }: IProps) => {
    const { t } = useTranslation();

    return (
        <>
            <div className={`TextUltraSmall ${styles.MainWrapper}`}>
                <div className={styles.MessageWrapper}>
                    <div className={styles.Badge}>{t("Dashboard.Languages.kotlinBadge")}</div>
                    <Trans
                        t={t}
                        i18nKey="Dashboard.Languages.noKotlin"
                        values={{ count: applicationsTotal }}
                        components={[<b key="0" />]}
                    />
                </div>

                {footerRight && <div className={styles.Date}>{footerRight}</div>}
            </div>
        </>
    );
};
