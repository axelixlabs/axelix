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
        return value;
    } else {
        return (
            <div className={styles.ValueWrapper}>
                <div>{t("ThreadDump.notAvailable")}</div>
                <InfoTooltip
                    text={`JVM does not track '${parameterName}' by default. 
            To track the '${parameterName}', you need to explicitly enable 
            thread contention monitoring`}
                />
            </div>
        );
    }
};

export default ThreadDumpTrackableValue;
