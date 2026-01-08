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
import { App } from "antd";
import { type MouseEvent } from "react";
import { useTranslation } from "react-i18next";

import styles from "./styles.module.css";

import CopyIcon from "assets/icons/copy.svg";

interface IProps {
    /**
     * Text that will be copied
     */
    text: string;
}

export const Copy = ({ text }: IProps) => {
    const { t } = useTranslation();
    const { message } = App.useApp();

    const handleCopy = async (e: MouseEvent<HTMLImageElement>): Promise<void> => {
        e.stopPropagation();

        try {
            await navigator.clipboard.writeText(text);
            message.success(t("copied"));
        } catch {
            message.error(t("copyFailed"));
        }
    };

    return <img src={CopyIcon} alt="Copy icon" onClick={handleCopy} className={styles.CopyIcon} />;
};
