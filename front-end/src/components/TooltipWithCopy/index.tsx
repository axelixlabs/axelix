import { Tooltip } from "antd";

import { Copy } from "../Copy";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Tooltip text
     */
    text: string;
    /**
     * Different onclick handlers on tooltip text
     */
    onClick?: () => void;
}

export const TooltipWithCopy = ({ text, onClick }: IProps) => {
    return (
        <>
            <Tooltip title={text} className={styles.Tooltip}>
                <div className={styles.TextWrapper}>
                    <div className={styles.Text} onClick={onClick}>
                        {text}
                    </div>
                    <Copy text={text} />
                </div>
            </Tooltip>
        </>
    );
};
