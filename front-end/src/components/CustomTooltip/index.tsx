import { type PropsWithChildren, type ReactNode, useEffect, useLayoutEffect, useRef, useState } from "react";

import { calculateTooltipPosition, isTooltipTruncatedText } from "helpers";
import type { ICustomTooltipState } from "models";

import { TooltipContent } from "./TooltipContent";
import { TooltipPortal } from "./TooltipPortal";
import { TooltipTrigger } from "./TooltipTrigger";

export interface IProps {
    content: ReactNode;
    checkTruncation?: boolean;
}

export const CustomTooltip = ({ children, content, checkTruncation = true }: PropsWithChildren<IProps>) => {
    const triggerRef = useRef<HTMLElement>(null);
    const tooltipRef = useRef<HTMLDivElement>(null);

    const showTimeout = useRef<number>(null);
    const hideTimeout = useRef<number>(null);

    const [isTooltipOpen, setIsTooltipOpen] = useState<boolean>(false);
    const [tooltipState, setTooltipState] = useState<ICustomTooltipState>({ top: 0, left: 0 });

    useEffect(() => {
        return () => {
            clearTimeout(showTimeout.current ?? undefined);
            clearTimeout(hideTimeout.current ?? undefined);
        };
    }, []);

    useLayoutEffect(() => {
        if (!isTooltipOpen) {
            return;
        }

        const refreshTooltipPosition = () => calculateTooltipPosition(triggerRef, tooltipRef, setTooltipState);

        refreshTooltipPosition();

        window.addEventListener("resize", refreshTooltipPosition);
        window.addEventListener("scroll", refreshTooltipPosition, true);

        return () => {
            window.removeEventListener("resize", refreshTooltipPosition);
            window.removeEventListener("scroll", refreshTooltipPosition, true);
        };
    }, [isTooltipOpen]);

    const handleShow = (): void => {
        const element = triggerRef.current;
        if (!element || (checkTruncation && !isTooltipTruncatedText(element))) {
            return;
        }

        if (hideTimeout.current) {
            clearTimeout(hideTimeout.current);
        }

        showTimeout.current = window.setTimeout(() => setIsTooltipOpen(true), 300);
    };

    const handleHide = (): void => {
        if (showTimeout.current) {
            clearTimeout(showTimeout.current);
        }

        hideTimeout.current = window.setTimeout(() => setIsTooltipOpen(false), 50);
    };

    return (
        <>
            <TooltipTrigger triggerRef={triggerRef} onShow={handleShow} onHide={handleHide}>
                {children}
            </TooltipTrigger>
            <TooltipPortal>
                <TooltipContent
                    tooltipRef={tooltipRef}
                    tooltipState={tooltipState}
                    isOpen={isTooltipOpen}
                    content={content}
                />
            </TooltipPortal>
        </>
    );
};
