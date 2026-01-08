/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import { App, Switch } from "antd";
import type { AxiosError } from "axios";
import { type MouseEvent, useState } from "react";
import * as React from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";

import { extractErrorCode } from "helpers";
import { type ICacheData, type IErrorResponse, StatelessRequest } from "models";
import { disableCache, enableCache } from "services";

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
    const { message } = App.useApp();
    const [mutationRequest, setMutationRequest] = useState(StatelessRequest.inactive());

    const switchTaskStatus = (e: MouseEvent<HTMLElement> | React.KeyboardEvent<HTMLButtonElement>) => {
        e.stopPropagation();
        setMutationRequest(StatelessRequest.loading());

        const requestBody = {
            instanceId: instanceId!,
            cacheManagerName: cacheManagerName,
            cacheName: cache.name,
        };

        (cache.enabled ? disableCache(requestBody) : enableCache(requestBody))
            .then(() => {
                message.success(t(`${cache.enabled ? "Caches.disabled" : "Caches.enabled"}`));
                cache.enabled = !cache.enabled;
                setMutationRequest(StatelessRequest.success());
            })
            .catch((error: AxiosError<IErrorResponse>) => {
                setMutationRequest(StatelessRequest.error(extractErrorCode(error?.response?.data)));
            });
    };

    return (
        <Switch
            checkedChildren={t("on")}
            unCheckedChildren={t("off")}
            onChange={(checked, event) => switchTaskStatus(event)}
            loading={mutationRequest.loading}
            checked={cache.enabled}
        />
    );
};
