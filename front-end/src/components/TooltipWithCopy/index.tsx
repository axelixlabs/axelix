import { message, Tooltip } from "antd";
import { useTranslation } from "react-i18next";

import styles from "./styles.module.css";

import CopyIcon from "assets/icons/copy.svg";

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

    const handleCopy = async (e: React.MouseEvent<HTMLImageElement>): Promise<void> => {
        e.stopPropagation();

        try {
            await navigator.clipboard.writeText(text);
            message.success(t("copied"));
        } catch {
            message.error(t("copyFailed"));
        }
    };

    return (
        <>
            <Tooltip
                title={text}
                styles={{
                    root: {
                        maxWidth: 600,
                        whiteSpace: "normal",
                    }
                }}
                className={styles.Tooltip}
            >
                <div className={styles.TextWrapper}>
                    <div className={styles.Text} onClick={onClick}>
                        {text}
                    </div>
                    <img
                        src={CopyIcon}
                        alt="Copy icon"
                        onClick={handleCopy}
                        className={styles.CopyIcon}
                    />
                </div>
            </Tooltip>
        </>
    );
};
