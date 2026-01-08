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
            targetScheduledTask: runnable.runnable.target,
        })
            .then(() => {
                message.success(t(`${runnable.enabled ? "ScheduledTasks.disabled" : "ScheduledTasks.enabled"}`));
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
