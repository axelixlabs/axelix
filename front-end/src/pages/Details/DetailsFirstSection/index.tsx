import { Button, List, Modal, Switch } from "antd";
import { useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";

import { detailsDownloadStateComponents } from "utils";

import styles from "./styles.module.css";

import DownloadIcon from "assets/icons/download.svg";

interface IProps {
    /**
     * The name of the instance
     */
    instanceName: string;
}

export const DetailsHeader = ({ instanceName }: IProps) => {
    const { instanceId } = useParams();
    const { t } = useTranslation();

    const [isModalOpen, setIsModalOpen] = useState<boolean>(false);
    const [stateComponents, setStateComponents] = useState<string[]>([]);

    const showModal = (): void => {
        setIsModalOpen(true);
    };

    const handleOk = (): void => {
        setIsModalOpen(false);
        const baseURL = `${import.meta.env.VITE_APP_API_URL}/api/axile/export-state/${instanceId}`;
        window.location.href = stateComponents.length ? `${baseURL}?components=${stateComponents.join(",")}` : baseURL;
    };

    const handleCancel = (): void => {
        setIsModalOpen(false);
    };

    const handleChange = (stateComponent: string): void => {
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
                icon={<img src={DownloadIcon} alt="Download icon" className={styles.DownloadIcon} />}
                onClick={showModal}
                className={styles.Download}
            >
                {t("Details.downloadState")}
            </Button>
            <Modal
                title={t("Details.exportConfiguration")}
                cancelText={t("cancel")}
                open={isModalOpen}
                onOk={handleOk}
                onCancel={handleCancel}
                centered
            >
                <List
                    header={<div className={styles.ListHeader}>{t("Details.exportConfiguration")}</div>}
                    bordered
                    dataSource={detailsDownloadStateComponents}
                    renderItem={(component) => (
                        <List.Item actions={[<Switch onChange={() => handleChange(component)} />]}>
                            {t(`Details.Components.${component}`)}
                        </List.Item>
                    )}
                />
            </Modal>
        </div>
    );
};
