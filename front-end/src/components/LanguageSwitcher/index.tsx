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
import { Select } from "antd";
import { useTranslation } from "react-i18next";

import styles from "./styles.module.css";

export const LanguageSwitcher = () => {
    const { i18n } = useTranslation();

    const handleChange = (value: string): void => {
        i18n.changeLanguage(value);
    };

    return (
        <Select
            defaultValue={i18n.language}
            onChange={handleChange}
            options={[
                { value: "en", label: "English" },
                { value: "ru", label: "Русский" },
            ]}
            className={styles.LanguageSwitcherSelect}
        />
    );
};
