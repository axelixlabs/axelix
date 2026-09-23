import { BASE_PATH } from "@/lib/constants.mjs";

/**
 * Axelix wordmark. The two files differ only in the colour of the letters, so the
 * variant is picked by the theme class rather than by JavaScript.
 *
 * TODO: custom — no built-in Fumadocs logo/wordmark component.
 */
export const AxelixLogo = () => (
    <>
        <img
            src={`${BASE_PATH}/img/logo.svg`}
            alt="Axelix"
            width={98}
            height={26}
            // The artwork's baseline sits a few px below the wordmark text (the round icon
            // hangs lower), which reads as "too low" next to the header's other text.
            className="h-[26px] w-auto -translate-y-[3px] dark:hidden"
        />
        <img
            src={`${BASE_PATH}/img/logo-dark.svg`}
            alt="Axelix"
            width={98}
            height={26}
            className="hidden h-[26px] w-auto -translate-y-[3px] dark:block"
        />
    </>
);
