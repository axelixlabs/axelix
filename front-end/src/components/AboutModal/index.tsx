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
import { FileTextOutlined, GithubOutlined, ReadOutlined } from "@ant-design/icons";

import { Button, Modal } from "antd";
import type { Dispatch, SetStateAction } from "react";
import { Trans, useTranslation } from "react-i18next";

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
    const corporateSupportEmail = import.meta.env.VITE_CORPORATE_SUPPORT_EMAIL;

    const onClose = (): void => {
        setOpen(false);
    };

    return (
        <Modal
            open={open}
            onOk={onClose}
            onCancel={onClose}
            centered
            width={550}
            cancelButtonProps={{
                style: { display: "none" },
            }}
        >
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
                        components={[
                            <a key="0" href={referenceGuideLink} target="_blank" rel="noopener noreferrer" />,
                            <a key="1" href={corporateSupportEmail} />,
                        ]}
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
                    {"About.githubSource"}
                </Button>

                <Button size="small" shape="round" icon={<ReadOutlined />} href={referenceGuideLink} target="_blank">
                    {t("documentation")}
                </Button>

                <Button icon={<FileTextOutlined />} size="small" shape="round" href={blogLink} target="_blank">
                    {t("About.blog")}
                </Button>
            </div>
        </Modal>
    );
}
