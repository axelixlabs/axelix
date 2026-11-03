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

import type { IAuthorityDelta } from "../../permissions";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Name used in the gain and loss headings
     */
    givenName: string;

    /**
     * Authorities added and removed by the pending role change
     */
    delta: IAuthorityDelta;
}

export const AuthorityDelta = ({ givenName, delta }: IProps) => {
    const { t } = useTranslation();

    if (delta.gained.length === 0 && delta.lost.length === 0) {
        return null;
    }

    return (
        <div className={styles.Delta}>
            {delta.gained.length > 0 && (
                <div>
                    <div className={styles.GainTitle}>
                        {t("Users.Profile.gain", { name: givenName, count: delta.gained.length })}
                    </div>
                    {delta.gained.map((authority) => (
                        <div key={authority} className={styles.GainRow}>
                            <span>+</span>
                            <span>
                                {t(`Users.Profile.Authorities.${authority}`)}
                                <span className={styles.Code}>{authority}</span>
                            </span>
                        </div>
                    ))}
                </div>
            )}
            {delta.lost.length > 0 && (
                <div>
                    <div className={styles.LoseTitle}>
                        {t("Users.Profile.lose", { name: givenName, count: delta.lost.length })}
                    </div>
                    {delta.lost.map((authority) => (
                        <div key={authority} className={styles.LoseRow}>
                            <span>−</span>
                            <span>
                                {t(`Users.Profile.Authorities.${authority}`)}
                                <span className={styles.Code}>{authority}</span>
                            </span>
                        </div>
                    ))}
                    {delta.retainedThrough && (
                        <p className={styles.Retained}>
                            {t("Users.Profile.staysGranted", { role: delta.retainedThrough })}
                        </p>
                    )}
                </div>
            )}
        </div>
    );
};
