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
import { Button } from "antd";
import { type Dispatch, type SetStateAction, useState } from "react";

import { PageSearch } from "components";

import { GlobalSlidingTimeLine } from "../GlobalSlidingTimeLine";
import { ThreadDumpSettingsModal } from "../ThreadDumpSettingsModal";

import styles from "./styles.module.css";

import { SettingsIcon } from "assets";

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
                isModalOpen={isModalOpen}
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
