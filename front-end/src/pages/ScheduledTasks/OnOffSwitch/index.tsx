import { Switch, message } from "antd";
import { useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";

import { type IRunnable, StatelessRequest } from "models";
import { updateScheduledTasksStatus } from "services";

interface IProps {
    /**
     * Any runnable task that can be turned off or turned on
     */
    runnable: IRunnable;
}

export const OnOffSwitch = ({ runnable }: IProps) => {
    const { t } = useTranslation();
    const { instanceId } = useParams();
    const [messageApi, contextHolder] = message.useMessage();
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
                messageApi.success(t(`${runnable.enabled ? "ScheduledTasks.disabled" : "ScheduledTasks.enabled"}`));
                runnable.enabled = !runnable.enabled;
                setMutationRequest(StatelessRequest.success());
            })
            .catch(() => setMutationRequest(StatelessRequest.error(""))); // TODO: Error handling
    };

    return (
        <>
            {contextHolder}
            <Switch
                checkedChildren={t("ScheduledTasks.on")}
                unCheckedChildren={t("ScheduledTasks.off")}
                onChange={() => switchTaskStatus()}
                loading={mutationRequest.loading}
                checked={runnable.enabled}
            />
        </>
    );
};
