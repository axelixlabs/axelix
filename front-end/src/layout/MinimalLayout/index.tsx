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
import { Outlet } from "react-router-dom";

import { LanguageSwitcher } from "components";
import LogoIcon from "src/assets/icons/logo.png";

import styles from "./styles.module.css";

export const MinimalLayout = () => {
    return (
        <div className={styles.MainWrapper}>
            <div className={styles.Header}>
                <div className="MainContainer">
                    <div className={styles.LanguageSwitcherWrapper}>
                        <img src={LogoIcon} alt="Axelix logo" className={styles.Logo} />
                        <LanguageSwitcher />
                    </div>
                </div>
            </div>
            <div className={styles.ContentWrapper}>
                <Outlet />
            </div>
        </div>
    );
};
