import { Button } from "antd";
import { useState } from "react";
import { useTranslation } from "react-i18next";

import { DetailsDownloadStateModal } from "../DetailsDownloadStateModal";

import styles from "./styles.module.css";

import DownloadIcon from "assets/icons/download.svg";

interface IProps {
    /**
     * The name of the instance
     */
    instanceName: string;
}

export const DetailsHeader = ({ instanceName }: IProps) => {
    const { t } = useTranslation();

    const [isModalOpen, setIsModalOpen] = useState<boolean>(false);

    return (
        <div className={styles.MainWrapper}>
            <div className={styles.MainTitle}>{instanceName}</div>
            <Button
                type="primary"
                icon={<img src={DownloadIcon} alt="Download icon" className={styles.DownloadIcon} />}
                onClick={() => setIsModalOpen(true)}
                className={styles.Download}
            >
                {t("Details.downloadState")}
            </Button>
            <DetailsDownloadStateModal isModalOpen={isModalOpen} setIsModalOpen={setIsModalOpen} />
        </div>
    );
};
