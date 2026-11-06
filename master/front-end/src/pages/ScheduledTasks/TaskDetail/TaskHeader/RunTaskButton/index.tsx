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
import { App, Button } from "antd";
import type { AxiosError } from "axios";
import { useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router";

import { RunIcon } from "@/assets";
import { NoRequiredAuthorityTooltip } from "@/components";
import { extractErrorCode } from "@/helpers";
import { useAuthority, useConfirmableAction } from "@/hooks";
import { EAuthorities, type IErrorResponse, StatelessRequest } from "@/models";
import { forceRunTask } from "@/services";

interface IProps {
    trigger: string;
}

export const RunTaskButton = ({ trigger }: IProps) => {
    const scheduledTasksAccess = useAuthority(EAuthorities.SCHEDULED_TASKS_MODIFY);

    const { t } = useTranslation();
    const { instanceId } = useParams();
    const { message } = App.useApp();
    const confirmAction = useConfirmableAction();

    const [forceRunRequest, setForceRunRequest] = useState(StatelessRequest.inactive());

    const runTask = (): void => {
        confirmAction({
            title: t("ScheduledTasks.runThisTaskTitle"),
            content: t("ScheduledTasks.runThisTaskDescription"),
            onOk() {
                setForceRunRequest(StatelessRequest.loading());

                forceRunTask({ instanceId: instanceId!, trigger: trigger })
                    .then(() => {
                        setForceRunRequest(StatelessRequest.success());
                        message.success(t("ScheduledTasks.runSuccess"));
                    })
                    .catch((error: AxiosError<IErrorResponse>) => {
                        setForceRunRequest(StatelessRequest.error(extractErrorCode(error?.response?.data)));
                    });
            },
        });
    };

    return (
        <NoRequiredAuthorityTooltip disabled={!scheduledTasksAccess}>
            <Button
                icon={<RunIcon />}
                type="primary"
                onClick={runTask}
                loading={forceRunRequest.loading}
                disabled={!scheduledTasksAccess}
            >
                {t("ScheduledTasks.runNow")}
            </Button>
        </NoRequiredAuthorityTooltip>
    );
};
