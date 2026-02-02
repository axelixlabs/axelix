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
import { FileTextOutlined, GithubOutlined, ReadOutlined } from "@ant-design/icons";

import { Button } from "antd";
import type { Dispatch, SetStateAction } from "react";
import { Trans, useTranslation } from "react-i18next";

import { UniversalModal } from "components";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Whether the modal is open
     */
    open: boolean;

    /**
     * Setter for the modal open state
     */
    setOpen: Dispatch<SetStateAction<boolean>>;
}

export function AboutModal({ open, setOpen }: IProps) {
    const { t } = useTranslation();

    const version = import.meta.env.VITE_APP_VERSION;
    const sourceCodeLink = import.meta.env.VITE_APP_SOURCE_CODE_LINK;
    const licenseLink = import.meta.env.VITE_APP_LICENSE_LINK;
    const referenceGuideLink = import.meta.env.VITE_APP_REFERENCE_GUIDE_LINK;
    const blogLink = import.meta.env.VITE_APP_BLOG_LINK;

    // TODO: Can we move that logic into the UniversalModal as well? Like, onClose
    // is almost always by default going to close the modal
    const onClose = (): void => {
        setOpen(false);
    };

    return (
        <UniversalModal open={open} onOk={onClose} onClose={onClose} displayCancel={false}>
            <div className={styles.HeaderWrapper}>
                <p className="TextMedium">{t("About.title")}</p>
                <p className={styles.Version}>
                    {t("About.version")}: {version}
                </p>
            </div>

            <div>
                <p className={styles.ParagraphGutter}>
                    <Trans t={t} i18nKey={"About.intro"} components={[<b key="0" />]} />
                </p>

                <p className={styles.ParagraphGutter}>
                    <Trans
                        t={t}
                        i18nKey={"About.licensing"}
                        components={[
                            <b key="0" />,
                            <a key="1" href={licenseLink} target="_blank" rel="noopener noreferrer" />,
                            <a key="2" href={sourceCodeLink} target="_blank" rel="noopener noreferrer" />,
                        ]}
                    />
                </p>

                <p className={styles.ParagraphGutter}>
                    <Trans
                        t={t}
                        i18nKey={"About.contact"}
                        components={[<a key="0" href={referenceGuideLink} target="_blank" rel="noopener noreferrer" />]}
                    />
                </p>

                <p className={styles.ParagraphGutter}>{t("About.bug")}</p>
            </div>

            <div className={styles.ActionsWrapper}>
                <Button
                    size="small"
                    shape="round"
                    icon={<GithubOutlined />}
                    href={sourceCodeLink}
                    target="_blank"
                    rel="noopener noreferrer"
                >
                    {t("About.githubSource")}
                </Button>

                <Button size="small" shape="round" icon={<ReadOutlined />} href={referenceGuideLink} target="_blank">
                    {t("documentation")}
                </Button>

                <Button icon={<FileTextOutlined />} size="small" shape="round" href={blogLink} target="_blank">
                    {t("About.blog")}
                </Button>
            </div>
        </UniversalModal>
    );
}
