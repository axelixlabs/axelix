import { type PropsWithChildren } from "react";
import { createPortal } from "react-dom";

export const TooltipPortal = ({ children }: PropsWithChildren) => {
    return createPortal(children, document.body);
};
