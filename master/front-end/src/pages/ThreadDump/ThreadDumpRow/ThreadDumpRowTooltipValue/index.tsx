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

import { InfoTooltip } from "components";

import styles from "./styles.module.css";

interface IProps {
    /**
     * The name of the parameter
     */
    parameterName: string;

    /**
     * Current thread dump parameter value
     */
    value: number;
}

const ThreadDumpTrackableValue = ({ value, parameterName }: IProps) => {
    const { t } = useTranslation();

    if (value != -1) {
        return <>{value}</>;
    } else {
        return (
            <>
                <div className={styles.ValueWrapper}>
                    <div>{t("ThreadDump.notAvailable")}</div>
                    <InfoTooltip
                        text={`JVM does not track '${parameterName}' by default. 
            To track the '${parameterName}', you need to explicitly enable 
            thread contention monitoring`}
                    />
                </div>
            </>
        );
    }
};

export default ThreadDumpTrackableValue;
