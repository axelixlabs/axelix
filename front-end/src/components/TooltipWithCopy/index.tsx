import { Copy } from "../Copy";
import { CustomTooltip } from "../CustomTooltip";

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
        <div className={styles.MainWrapper}>
            <CustomTooltip content={text}>
                <div className={styles.Text} onClick={onClick}>
                    {text}
                </div>
            </CustomTooltip>
            <Copy text={text} />
        </div>
    );
};
