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
import { Button } from "antd";
import SettingsIcon from "assets/icons/settings.svg?react";
import { type Dispatch, type SetStateAction, useState } from "react";

import { PageSearch } from "components";

import { GlobalSlidingTimeLine } from "../GlobalSlidingTimeLine";
import { ThreadDumpSettingsModal } from "../ThreadDumpSettingsModal";

import styles from "./styles.module.css";

interface IProps {
    /**
     * SetState to update the search
     */
    setSearch: Dispatch<SetStateAction<string>>;

    /**
     * Тext to display after the search field
     */
    addonAfter: string;

    /**
     * Whether thread contention monitoring is enabled
     */
    contentionMonitoring: boolean;
}

export const ThreadDumpFirstSection = ({ setSearch, addonAfter, contentionMonitoring }: IProps) => {
    const [isModalOpen, setIsModalOpen] = useState<boolean>(false);

    return (
        <>
            <ThreadDumpSettingsModal
                isModalopen={isModalOpen}
                setIsModalOpen={setIsModalOpen}
                contentionMonitoring={contentionMonitoring}
            />
            {/* Empty attribute required for the correct styling to be applied, see MainLayout component styling */}
            <div data-thread-layout className={styles.FirstSectionWrapper}>
                <div className={styles.SearchAndSettingsWrapper}>
                    <PageSearch setSearch={setSearch} addonAfter={addonAfter} removeBottomGutter />
                    <Button icon={<SettingsIcon />} type="primary" onClick={() => setIsModalOpen(true)} />
                </div>
                <GlobalSlidingTimeLine />
            </div>
        </>
    );
};
