import { message, Tooltip } from "antd";
import { useTranslation } from "react-i18next";
import { CopyOutlined } from "@ant-design/icons";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Tooltip text
     */
    text: string;
    /**
     * Different onclick handlers on tooltip text
     */
    onClick?: () => void
}

export const TooltipWithCopy = ({ text, onClick }: IProps) => {
    const { t } = useTranslation();

    const handleCopy = (copyText: string): void => {
        navigator.clipboard.writeText(copyText);
        message.success(t("copied"));
    };

    return (
        <div className={styles.TextWrapper}>
            <Tooltip
                title={text}
                styles={{
                    root: {
                        maxWidth: 600,
                        whiteSpace: "normal",
                    }
                }}
            >
                <div className={styles.Text} onClick={onClick}>
                    {text}
                </div>
            </Tooltip>
            <CopyOutlined
                onClick={(e) => {
                    e.stopPropagation();
                    handleCopy(text);
                }}
                className={styles.CopyIcon}
            />
        </div>
    );
};
