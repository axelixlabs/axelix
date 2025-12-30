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
import { useNavigate } from "react-router-dom";

import { LanguageSwitcher } from "components";

import { Help } from "./Help";
import { NavigationBar } from "./NavigationBar";
import { ProfileMenu } from "./ProfileMenu";
import styles from "./styles.module.css";

import LogoIcon from "assets/icons/logo.png";

export const AdminHeader = () => {
    const navigate = useNavigate();

    return (
        <div className={styles.Header}>
            <img src={LogoIcon} alt="Axelix logo" onClick={() => navigate("/wallboard")} className={styles.Logo} />
            <div className={styles.LinksAndAvatarWrapper}>
                <NavigationBar />
                <Help />
                <ProfileMenu />
                <LanguageSwitcher />
            </div>
        </div>
    );
};
