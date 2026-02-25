/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
                message.success(cache.enabled ? t("Caches.disabled") : t("Caches.enabled"));
                cache.enabled = !cache.enabled;
                setMutationRequest(StatelessRequest.success());
            })
            .catch((error: AxiosError<IErrorResponse>) => {
                setMutationRequest(StatelessRequest.error(extractErrorCode(error?.response?.data)));
            });
    };

    return (
        <>
            <Switch
                checkedChildren={t("on")}
                unCheckedChildren={t("off")}
                onChange={(checked, event) => switchTaskStatus(event)}
                loading={mutationRequest.loading}
                checked={cache.enabled}
            />
        </>
    );
};
