import { useTranslation } from "react-i18next";

import { Accordion } from "components";
import type { ICachesManager } from "models";

import { CacheAccordionHeader } from "../CacheAccordionHeader";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Single cache manager data
     */
    cacheManager: ICachesManager;
}

export const CacheManagerSection = ({ cacheManager }: IProps) => {
    const { t } = useTranslation();

    return (
        <div className={styles.CacheManagerWrapper}>
            <div className="CustomizedAntdTable">
                <div className={`TableHeader ${styles.CacheManagerHeader}`}>
                    <div className={`RowChunk ${styles.CacheManagerName}`}>
                        {t("Caches.name")}: {cacheManager.name}
                    </div>
                    <div className={`RowChunk ${styles.RowChunk}`}>{t("Caches.clear")}</div>
                    <div className={`RowChunk ${styles.RowChunk}`}>{t("status")}</div>
                </div>
                {cacheManager.caches.map((cache) => (
                    <Accordion
                        header={<CacheAccordionHeader cacheManagerName={cacheManager.name} cache={cache} />}
                        headerStyles={styles.HeaderStyles}
                        children={<></>}
                        key={cache.name}
                    />
                ))}
            </div>
        </div>
    );
};
