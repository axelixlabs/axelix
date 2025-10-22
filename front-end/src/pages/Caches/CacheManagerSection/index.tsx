import { Collapse } from "antd";
import { type CollapseProps } from "antd";
import classNames from "classnames";
import { useState } from "react";

import type { ICachesManager } from "models";

import { CacheCollapseHeader } from "../CacheCollapseHeader";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Single cache manager data
     */
    cacheManager: ICachesManager;
}

export const CacheManagerSection = ({ cacheManager }: IProps) => {
    const [activeKey] = useState<string | string[]>([]);

    const createCollapseItems = (): CollapseProps["items"] => {
        return cacheManager.caches.map((cache) => ({
            key: cache.name,
            label: <CacheCollapseHeader cacheManagerName={cacheManager.name} cache={cache} />,
            // todo add body in future
            children: <></>,
        }));
    };

    return (
        <div className={styles.CacheManagerWrapper}>
            <div className={styles.CacheManagerTopSection}>
                <div className={classNames("MediumTitle", styles.CacheManagerName)}>{cacheManager.name}</div>
            </div>
            <Collapse
                accordion
                activeKey={activeKey}
                items={createCollapseItems()}
                // todo add handler in future
                // onChange={(key) => setActiveKey(key)}
            />
        </div>
    );
};
