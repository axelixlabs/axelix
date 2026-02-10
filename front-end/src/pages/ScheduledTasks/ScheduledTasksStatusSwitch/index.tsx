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
import { useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";

import { extractErrorCode } from "helpers";
import { type IErrorResponse, type IRunnable, StatelessRequest } from "models";
import { updateScheduledTasksStatus } from "services";

interface IProps {
    /**
     * Any runnable task that can be turned off or turned on
     */
    runnable: IRunnable;
}

export const ScheduledTasksStatusSwitch = ({ runnable }: IProps) => {
    const { t } = useTranslation();
    const { instanceId } = useParams();
    const { message } = App.useApp();

    const [mutationRequest, setMutationRequest] = useState(StatelessRequest.inactive());

    const switchTaskStatus = () => {
        setMutationRequest(StatelessRequest.loading());

        updateScheduledTasksStatus({
            force: false,
            instanceId: instanceId!,
            statusType: runnable.enabled ? "disable" : "enable",
            trigger: runnable.runnable.target,
        })
            .then(() => {
                message.success(runnable.enabled ? t("ScheduledTasks.disabled") : t("ScheduledTasks.enabled"));
                runnable.enabled = !runnable.enabled;
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
            onChange={() => switchTaskStatus()}
            loading={mutationRequest.loading}
            checked={runnable.enabled}
        />
    );
};
