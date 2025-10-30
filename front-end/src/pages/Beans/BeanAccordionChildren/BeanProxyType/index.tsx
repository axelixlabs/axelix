import { useTranslation } from "react-i18next";

import { resolveProxying } from "helpers/beans";
import { EProxyType } from "models";

import styles from "../styles.module.css";

interface IProps {
    /**
     * The proxying algorithm used to create the instance of the bean. Might be null
     * in case the backend was unable to figure it out.
     */
    proxyType: EProxyType | null;
}

export const BeanProxyType = ({ proxyType }: IProps) => {
    const { t } = useTranslation();

    return (
        <>
            <div className={styles.AccordionBodyChunkTitle}>{t("Beans.beanProxyType")}:</div>
            <span>{resolveProxying(t, proxyType)}</span>
        </>
    );
};
