import type { ReactNode, RefObject } from "react";

import type { ICustomTooltipState } from "models";

import styles from "./styles.module.css";

interface IProps {
    tooltipRef: RefObject<HTMLDivElement | null>;
    tooltipState: ICustomTooltipState;
    isOpen: boolean;
    content: ReactNode;
}

export const TooltipContent = ({ tooltipRef, tooltipState, isOpen, content }: IProps) => {
    return (
        <div
            ref={tooltipRef}
            className={`${styles.TooltipContentWrapper} ${isOpen ? styles.TooltipOpened : ""}`}
            style={{ top: `${tooltipState.top}px`, left: `${tooltipState.left}px`, maxWidth: `${600}px` }}
        >
            <div className={styles.TooltipContent}>{content}</div>
            <div className={styles.TooltipArrow} />
        </div>
    );
};
