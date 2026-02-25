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
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";

import { extractErrorCode } from "helpers";
import { type IErrorResponse, StatelessRequest } from "models";
import { disableContentionMonitoring, enableContentionMonitoring } from "services";

interface IProps {
    /**
     * Whether thread contention monitoring is enabled
     */
    contentionMonitoring: boolean;
}

export const ContentionMonitoringStatusSwitch = ({ contentionMonitoring }: IProps) => {
    const { t } = useTranslation();
    const { instanceId } = useParams();
    const { message } = App.useApp();

    const [mutationRequest, setMutationRequest] = useState(StatelessRequest.inactive());

    const switchTaskStatus = (e: MouseEvent<HTMLElement> | React.KeyboardEvent<HTMLButtonElement>) => {
        e.stopPropagation();
        setMutationRequest(StatelessRequest.loading());

        (contentionMonitoring ? disableContentionMonitoring(instanceId!) : enableContentionMonitoring(instanceId!))
            .then(() => {
                message.success(
                    contentionMonitoring
                        ? t("ThreadDump.Settings.contentionMonitoringDisabled")
                        : t("ThreadDump.Settings.contentionMonitoringEnabled"),
                );
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
                onChange={(_, event) => switchTaskStatus(event)}
                loading={mutationRequest.loading}
                checked={contentionMonitoring}
            />
        </>
    );
};
