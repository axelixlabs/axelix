import { Switch, message } from "antd";
import type { AxiosError } from "axios";
import { useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";

import { extractErrorCode } from "helpers";
import { type ICacheData, type IErrorResponse, StatelessRequest } from "models";
import { updateCacheStatus } from "services";

interface IProps {
    /**
     * Name of the cache manager
     */
    cacheManagerName: string;
    /**
     * Single cache data
     */
    cache: ICacheData;
}

export const CacheStatusSwitch = ({ cacheManagerName, cache }: IProps) => {
    const { t } = useTranslation();
    const { instanceId } = useParams();
    const [messageApi, contextHolder] = message.useMessage();
    const [mutationRequest, setMutationRequest] = useState(StatelessRequest.inactive());

    const switchTaskStatus = () => {
        setMutationRequest(StatelessRequest.loading());

        updateCacheStatus({
            instanceId: instanceId!,
            cacheManagerName: cacheManagerName,
            cacheName: cache.name,
            statusType: cache.enabled ? "disable" : "enable",
        })
            .then(() => {
                messageApi.success(t(`${cache.enabled ? "Caches.disabled" : "Caches.enabled"}`));
                cache.enabled = !cache.enabled;
                setMutationRequest(StatelessRequest.success());
            })
            .catch((error: AxiosError<IErrorResponse>) => {
                setMutationRequest(StatelessRequest.error(extractErrorCode(error?.response?.data)));
            });
    };

    return (
        <>
            {contextHolder}
            <Switch
                checkedChildren={t("on")}
                unCheckedChildren={t("off")}
                onChange={() => switchTaskStatus()}
                loading={mutationRequest.loading}
                checked={cache.enabled}
            />
        </>
    );
};
