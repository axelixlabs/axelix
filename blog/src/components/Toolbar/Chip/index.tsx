import type {CSSProperties, ReactNode} from "react";
import styles from "./styles.module.css";

interface IProps {
    children: ReactNode;
    active?: boolean;
    style?: CSSProperties;
}

export const Chip = ({children, active = false, style}: IProps) => {
    return (
        <>
            <span
                className={`${styles.Chip}${active ? ` ${styles.ActiveChip}` : ""}`}
                style={style}
            >
                <span className={styles.ChipDot}/>
                {children}
            </span>
        </>
    );
};