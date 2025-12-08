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
// TODO: Comment out the code after we decide to restore the logic for adding and removing profiles.
// import { CheckOutlined, DeleteFilled, PlusOutlined } from "@ant-design/icons";
// import { Button, Input } from "antd";
// import { useState } from "react";
import { useTranslation } from "react-i18next";

import styles from "./styles.module.css";

interface IProps {
    /**
     * List of active profiles inside the given Spring Boot application
     */
    activeProfiles: string[];
}

export const EnvironmentProfiles = ({ activeProfiles }: IProps) => {
    const { t } = useTranslation();

    // const [createProfile, setCreateProfile] = useState(false);

    return (
        <div className={styles.MainWrapper}>
            <div className={styles.ProfilesWrapper}>
                <div className={styles.ProfileTitle}>{t("Environments.activeProfiles")}</div>
                {activeProfiles.map((activeProfile) => (
                    <div className={styles.ProfileWrapper} key={activeProfile}>
                        <div className={styles.ProfileValue}>
                            {activeProfile}
                            {/* <DeleteFilled className={styles.DeleteActiveProfileIcon} /> */}
                        </div>
                    </div>
                ))}
                {/* <div>
                    {createProfile ? (
                        <div className={styles.CreateProfileWrapper}>
                            <Input className={styles.CreateProfileInput} />
                            <Button
                                icon={<CheckOutlined />}
                                type="primary"
                                className={styles.CreateProfileActionsButton}
                            />
                        </div>
                    ) : (
                        <Button icon={<PlusOutlined />} type="primary" onClick={() => setCreateProfile(true)} />
                    )}
                </div> */}
            </div>
        </div>
    );
};
