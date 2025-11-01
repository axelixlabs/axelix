import { Button } from "antd";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";

import styles from "./styles.module.css";

import DownloadIcon from "assets/icons/download.svg";

interface IProps {
    /**
     * The name of the service
     */
    serviceName: string;
}

export const DetailsFirstSection = ({ serviceName }: IProps) => {
    const { instanceId } = useParams();
    const { t } = useTranslation();

    return (
        <div className={styles.MainWrapper}>
            <div className={styles.MainTitle}>{serviceName}</div>
            <Button
                type="primary"
                icon={<img src={DownloadIcon} alt="Download icon" className={styles.DownloadIcon} />}
                href={`${import.meta.env.VITE_APP_API_URL}/api/axile/export-state/${instanceId}`}
                className={styles.Download}
            >
                {t("Details.downloadState")}
            </Button>
        </div>
    );
};
