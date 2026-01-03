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
import { BookOutlined, CommentOutlined } from "@ant-design/icons";

import { Dropdown, type MenuProps } from "antd";
import AboutIcon from "assets/icons/info-black.svg";
import { useState } from "react";
import { useTranslation } from "react-i18next";

import { AboutModal } from "components";

import styles from "./styles.module.css";

export const Help = () => {
    const { t } = useTranslation();

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
            // TODO: Replace this via svgr in future and delete the info-black.svg icon
            icon: <img src={AboutIcon} alt="About icon" />,
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
            <Dropdown menu={{ items }}>
                <a onClick={(e) => e.preventDefault()} className={styles.HelpLabelWrapper}>
                    <div className={styles.HelpLabel}>
                        {t("Header.Help.title")}

                        {/* TODO: Replace this in future via svgr */}
                        <svg
                            xmlns="http://www.w3.org/2000/svg"
                            width="16"
                            height="16"
                            viewBox="0 0 24 24"
                            strokeWidth="2"
                            stroke="currentColor"
                            fill="none"
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            className={styles.ArrowIcon}
                        >
                            <path stroke="none" d="M0 0h24v24H0z" fill="none" />
                            <polyline points="6 9 12 15 18 9" />
                        </svg>
                    </div>
                </a>
            </Dropdown>
            <AboutModal open={open} setOpen={setOpen} />
        </>
    );
};
