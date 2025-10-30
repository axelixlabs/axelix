import classNames from "classnames";

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
    return (
        <div className={styles.CacheManagerWrapper}>
            <div className={styles.CacheManagerTopSection}>
                <div className={classNames("MediumTitle", styles.CacheManagerName)}>{cacheManager.name}</div>
            </div>
            <div className="AccordionsWrapper">
                {cacheManager.caches.map((cache) => (
                    <Accordion
                        header={<CacheAccordionHeader cacheManagerName={cacheManager.name} cache={cache} />}
                        children={<></>}
                        key={cache.name}
                        // todo add handler in future
                        // onChange={(key) => setActiveKey(key)}
                    />
                ))}
            </div>
        </div>
    );
};
