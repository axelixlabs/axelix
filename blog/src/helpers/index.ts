import type {CSSProperties} from "react";

export const chipColorStyle  = (color: string): CSSProperties => {
    return ({["--chip" as string]: color})
};