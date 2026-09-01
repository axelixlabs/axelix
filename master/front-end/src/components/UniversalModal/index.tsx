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
import { Modal } from "antd";
import type { PropsWithChildren, ReactNode } from "react";
import { useTranslation } from "react-i18next";

import styles from "./styles.module.css";

export interface IProps {
    /** Whether the modal is visible */
    open: boolean;

    /** Callback when the OK button is clicked */
    onOk: () => void;

    /** Modal title */
    title: string;

    subtitle?: string;

    /** Callback when the modal is cancelled or closed */
    onClose?: () => void;

    /** Text for the OK button */
    okText?: string;

    /** Loading state for the OK button */
    loading?: boolean;

    /**
     * Whether to display the cancel button or not
     */
    displayCancel?: boolean;

    /**
     * Whether to display the okay button or not
     */
    displayOkay?: boolean;

    /**
     * Whether it is possible to close the Modal via clicking on the mask.
     */
    maskCloseable?: boolean;

    /**
     * Extra content rendered on the left side of the modal footer, next to the buttons.
     */
    footerExtra?: ReactNode;
}

export const UniversalModal = ({
    children,
    title,
    subtitle,
    open,
    onOk,
    onClose,
    okText,
    loading,
    displayCancel = true,
    displayOkay = true,
    maskCloseable = false,
    footerExtra,
}: PropsWithChildren<IProps>) => {
    const { t } = useTranslation();

    return (
        <>
            <Modal
                open={open}
                onOk={onOk}
                onCancel={onClose}
                centered
                width={550}
                okText={okText}
                cancelText={t("cancel")}
                loading={loading}
                cancelButtonProps={
                    !displayCancel
                        ? {
                              style: {
                                  display: "none",
                              },
                          }
                        : {}
                }
                okButtonProps={
                    !displayOkay
                        ? {
                              style: {
                                  display: "none",
                              },
                          }
                        : {}
                }
                mask={{
                    closable: maskCloseable,
                    blur: true,
                }}
                footer={footerExtra
                    ? (_, { OkBtn, CancelBtn }) => (
                          <div className={styles.Footer}>
                              <div>{footerExtra}</div>
                              <div className={styles.FooterButtons}>
                                  <CancelBtn />
                                  <OkBtn />
                              </div>
                          </div>
                      )
                    : undefined
                }
            >
                <div className={styles.Header}>
                    <div className="TextMedium">{title}</div>
                    {subtitle && <div className={styles.Subtitle}>{subtitle}</div>}
                </div>

                {children}
            </Modal>
        </>
    );
};
