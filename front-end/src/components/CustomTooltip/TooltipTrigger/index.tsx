import { type MouseEvent, type ReactNode, type RefCallback, type RefObject, cloneElement, isValidElement } from "react";

import type { ITooltipChildProps } from "models";

interface IProps {
    children: ReactNode;
    triggerRef: RefObject<HTMLElement | null>;
    onShow: () => void;
    onHide: () => void;
}

export const TooltipTrigger = ({ children, triggerRef, onShow, onHide }: IProps) => {
    if (isValidElement<ITooltipChildProps>(children)) {
        const element = children;

        const handleRef: RefCallback<HTMLElement> = (node) => {
            triggerRef.current = node;
        };

        const handleMouseEnter = (e: MouseEvent<HTMLElement>): void => {
            element.props.onMouseEnter?.(e);
            onShow();
        };

        const handleMouseLeave = (e: MouseEvent<HTMLElement>): void => {
            element.props.onMouseLeave?.(e);
            onHide();
        };

        return cloneElement(element, {
            ref: handleRef,
            onMouseEnter: handleMouseEnter,
            onMouseLeave: handleMouseLeave,
        });
    }

    return (
        <span ref={triggerRef} onMouseEnter={onShow} onMouseLeave={onHide}>
            {children}
        </span>
    );
};
