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
import { Menu } from "antd";
import { useTranslation } from "react-i18next";
import { useLocation, useNavigate, useParams } from "react-router-dom";

import { findOpenKeys } from "helpers";
import { getItems } from "utils";

import styles from "./styles.module.css";

export const SiderMenu = () => {
    const { t } = useTranslation();

    const navigate = useNavigate();
    const location = useLocation();
    const { instanceId } = useParams();

    return (
        <Menu
            mode="inline"
            items={getItems(instanceId!, t)}
            onClick={({ key }) => navigate(key)}
            selectedKeys={[location.pathname]}
            defaultOpenKeys={findOpenKeys(getItems(instanceId!, t))}
            className={styles.Menu}
        />
    );
};
