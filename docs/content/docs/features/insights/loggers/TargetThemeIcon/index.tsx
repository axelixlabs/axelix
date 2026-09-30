import { BASE_PATH } from "@/lib/constants.mjs";

import styles from "./styles.module.css";

/**
 * Target glyph used inline in the Loggers tables. The two files differ only in stroke colour,
 * so the variant is picked by the theme class rather than by JavaScript.
 */
export const TargetThemeIcon = () => (
    <>
        <img
            src={`${BASE_PATH}/img/feature/icons/target-dark.svg`}
            alt="Target icon"
            width={15}
            height={15}
            className={`${styles.TargetIcon} not-prose inline dark:hidden`}
        />
        <img
            src={`${BASE_PATH}/img/feature/icons/target-light.svg`}
            alt="Target icon"
            width={15}
            height={15}
            className={`${styles.TargetIcon} not-prose hidden dark:inline`}
        />
    </>
);
