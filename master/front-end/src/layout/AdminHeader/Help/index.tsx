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
import { BookOutlined, CommentOutlined } from "@ant-design/icons";

import { Dropdown, type MenuProps } from "antd";
import { useState } from "react";
import { useTranslation } from "react-i18next";

import { ArrowIcon, InfoIcon, LicenseIcon } from "@/assets";

import { HelpAboutItem } from "./HelpAboutItem";
import { HelpLicenseItem } from "./HelpLicenseItem";
import styles from "./styles.module.css";

export const Help = () => {
    const { t } = useTranslation();

    const [dropdownOpen, setDropdownOpen] = useState<boolean>(false);

    const version = import.meta.env.VITE_APP_VERSION;

    const items: MenuProps["items"] = [
        {
            key: "version",
            type: "group",
            label: `Axelix V${version}`,
        },
        {
            type: "divider",
        },
        {
            key: "documentation",
            icon: <BookOutlined className={styles.CommonIcon} />,
            label: (
                <a target="_blank" rel="noopener noreferrer" href="https://axelix.io/docs">
                    {t("documentation")}
                </a>
            ),
        },
        {
            key: "license",
            // TODO: Change the icon in the future
            icon: <LicenseIcon />,
            label: <HelpLicenseItem />,
        },
        {
            key: "about",
            icon: <InfoIcon />,
            label: <HelpAboutItem />,
        },
        {
            key: "feedback",
            icon: <CommentOutlined className={styles.CommonIcon} />,
            label: (
                <a target="_blank" rel="noopener noreferrer" href="https://github.com/axelixlabs/axelix/issues">
                    {t("Header.Help.feedback")}
                </a>
            ),
        },
    ];

    return (
        <>
            <Dropdown menu={{ items }} onOpenChange={(open) => setDropdownOpen(open)}>
                <div className={styles.HelpLabelWrapper}>
                    <div className={styles.HelpLabel}>
                        {t("Header.Help.title")}
                        <ArrowIcon className={`${styles.ArrowIcon} ${dropdownOpen ? styles.OpenArrowIcon : ""}`} />
                    </div>
                </div>
            </Dropdown>
        </>
    );
};
