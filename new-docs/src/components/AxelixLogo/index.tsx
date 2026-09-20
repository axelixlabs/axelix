import { BASE_PATH } from "@/lib/constants.mjs";

/**
 * Axelix wordmark. The two files differ only in the colour of the letters, so the
 * variant is picked by the theme class rather than by JavaScript.
 */
export const AxelixLogo = () => (
    <>
        <img
            src={`${BASE_PATH}/img/logo.svg`}
            alt="Axelix"
            width={98}
            height={26}
            className="h-[26px] w-auto dark:hidden"
        />
        <img
            src={`${BASE_PATH}/img/logo-dark.svg`}
            alt="Axelix"
            width={98}
            height={26}
            className="hidden h-[26px] w-auto dark:block"
        />
    </>
);
