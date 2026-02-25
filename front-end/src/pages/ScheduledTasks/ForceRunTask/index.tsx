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
import { useParams } from "react-router-dom";

import { extractErrorCode } from "helpers";
import { type IErrorResponse, StatelessRequest } from "models";
import { forceRunTask } from "services";

import { RunIcon } from "assets";

interface IProps {
    /**
     * Trigger for force run
     */
    trigger: string;
}

export const ForceRunTask = ({ trigger }: IProps) => {
    const { t } = useTranslation();

    const { instanceId } = useParams();
    const { message } = App.useApp();

    const [forceRunTaskData, setForceRunTaskData] = useState(StatelessRequest.inactive());

    const forceRunClickHandler = (): void => {
        setForceRunTaskData(StatelessRequest.loading());

        forceRunTask({
            instanceId: instanceId!,
            trigger: trigger,
        })
            .then(() => {
                setForceRunTaskData(StatelessRequest.success());
                message.success(t("ScheduledTasks.runSuccess"));
            })
            .catch((error: AxiosError<IErrorResponse>) => {
                setForceRunTaskData(StatelessRequest.error(extractErrorCode(error?.response?.data)));
            });
    };

    return (
        <>
            <Button
                icon={<RunIcon />}
                type="primary"
                onClick={forceRunClickHandler}
                loading={forceRunTaskData.loading}
            />
        </>
    );
};
