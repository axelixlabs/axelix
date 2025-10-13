import { useState } from 'react';
import { Collapse } from "antd";
import classNames from 'classnames';
import { Button, type CollapseProps } from 'antd';
import { ReloadOutlined } from "@ant-design/icons";

import { CacheCollapseHeader } from '../CacheCollapseHeader';
import type { ICachesManager } from 'models';

import styles from './styles.module.css'

interface IProps {
    /**
     * Single cache manager data
     */
    cacheManager: ICachesManager
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
                <div className={classNames('MediumTitle', styles.CacheManagerName)}>
                    {cacheManager.name}
                </div>
                <Button
                    icon={<ReloadOutlined />}
                    type="primary"
                    className={styles.ClearCachesButton}
                />
            </div>
            <Collapse
                accordion
                activeKey={activeKey}
                items={createCollapseItems()}
            // todo add handler in future
            // onChange={(key) => setActiveKey(key)}
            />
        </div>
    )
};