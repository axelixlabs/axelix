import { Button, List, Modal, Switch } from "antd";
import { useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";

import { EExportableComponent } from "models";
import { exportStateData } from "services";

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
    const [stateComponents, setStateComponents] = useState<EExportableComponent[]>([]);

    const showModal = (): void => {
        setIsModalOpen(true);
    };

    const handleOk = (): void => {
        setIsModalOpen(false);
        exportStateData({
            instanceId: instanceId!,
            body: {
                components: stateComponents.map((value) => {
                    return {
                        component: value,
                    };
                }),
            },
        });
    };

    const handleCancel = (): void => {
        setIsModalOpen(false);
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
                    bordered
                    dataSource={Object.values(EExportableComponent)}
                    renderItem={(component) => (
                        <List.Item actions={[<Switch onChange={() => handleChange(component)} />]}>
                            {t(`Details.Components.${component}`)}
                        </List.Item>
                    )}
                    className={styles.List}
                />
            </Modal>
        </div>
    );
};
