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

import { AboutModal } from "components";

import styles from "./styles.module.css";

import { ArrowIcon, InfoIcon } from "assets";

export const Help = () => {
    const { t } = useTranslation();

    const [dropdownOpen, setDropdownOpen] = useState(false);
    const [open, setOpen] = useState<boolean>(false);

    const version = import.meta.env.VITE_APP_VERSION;

    const items: MenuProps["items"] = [
        {
            key: "1",
            type: "group",
            label: `Axelix V${version}`,
        },
        {
            type: "divider",
        },
        {
            key: "2",
            icon: <BookOutlined className={styles.CommonIcon} />,
            label: (
                <a target="_blank" rel="noopener noreferrer" href="#">
                    {t("documentation")}
                </a>
            ),
        },
        {
            key: "3",
            icon: <InfoIcon />,
            label: <a onClick={() => setOpen(true)}>{t("Header.Help.about")}</a>,
        },
        {
            key: "4",
            icon: <CommentOutlined className={styles.CommonIcon} />,
            label: (
                <a target="_blank" rel="noopener noreferrer" href="#">
                    {t("Header.Help.feedback")}
                </a>
            ),
        },
    ];

    return (
        <>
            <Dropdown menu={{ items }} onOpenChange={(open) => setDropdownOpen(open)}>
                <a onClick={(e) => e.preventDefault()} className={styles.HelpLabelWrapper}>
                    <div className={styles.HelpLabel}>
                        {t("Header.Help.title")}
                        <ArrowIcon className={`${styles.ArrowIcon} ${dropdownOpen && styles.OpenArrowIcon}`} />
                    </div>
                </a>
            </Dropdown>
            <AboutModal open={open} setOpen={setOpen} />
        </>
    );
};
