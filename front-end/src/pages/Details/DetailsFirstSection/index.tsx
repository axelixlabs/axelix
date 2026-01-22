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
import { Button, List, Modal, Switch } from "antd";
import DownloadIcon from "assets/icons/download.svg?react";
import { useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";

import { Loader } from "components";
import { downloadFile } from "helpers";
import { EExportableComponent } from "models";
import { exportStateData } from "services";

import styles from "./styles.module.css";

interface IProps {
    /**
     * The name of the instance
     */
    instanceName: string;
}

export const DetailsHeader = ({ instanceName }: IProps) => {
    const { instanceId } = useParams();
    const { t } = useTranslation();

    const [isLoading, setLoading] = useState<boolean>(false);

    const [isModalOpen, setIsModalOpen] = useState<boolean>(false);
    const [stateComponents, setStateComponents] = useState<EExportableComponent[]>([]);

    const handleOk = async (): Promise<void> => {
        setLoading(true);

        exportStateData({
            instanceId: instanceId!,
            body: {
                components: stateComponents.map((value) => ({
                    component: value,
                })),
            },
        })
            .then((value) => {
                setIsModalOpen(false);
                // We have to manually download the file here since the request to the server is a POST http
                // request and therefore the browser might not catch up the possible Content-Disposition header
                downloadFile(value.data);
                setLoading(false);
            })
            .finally(() => {
                setStateComponents([]);
            });
    };

    const handleChange = (stateComponent: EExportableComponent): void => {
        setStateComponents((prev) =>
            prev.includes(stateComponent)
                ? prev.filter((component) => component !== stateComponent)
                : [...prev, stateComponent],
        );
    };

    return (
        <div className={styles.MainWrapper}>
            <div className={styles.MainTitle}>{instanceName}</div>
            <Button
                type="primary"
                icon={<DownloadIcon />}
                onClick={() => setIsModalOpen(true)}
                className={styles.Download}
            >
                {t("Details.downloadState")}
            </Button>
            <Modal
                title={isLoading ? t("Details.exportConfigurationLoading") : t("Details.exportConfigurationOptions")}
                cancelText={t("cancel")}
                open={isModalOpen}
                onOk={handleOk}
                onCancel={() => setIsModalOpen(false)}
                centered
                okButtonProps={{ disabled: isLoading }}
                cancelButtonProps={{ disabled: isLoading }}
            >
                {isLoading ? (
                    <div className={styles.LoaderWrapper}>
                        <Loader />
                    </div>
                ) : (
                    <List
                        bordered
                        dataSource={Object.values(EExportableComponent)}
                        renderItem={(component) => (
                            <List.Item actions={[<Switch onChange={() => handleChange(component)} />]}>
                                {t(`Details.Components.${component}`)}
                            </List.Item>
                        )}
                        className={styles.List}
                    />
                )}
            </Modal>
        </div>
    );
};
