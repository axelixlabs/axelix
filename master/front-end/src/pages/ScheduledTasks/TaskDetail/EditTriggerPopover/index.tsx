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
import { EditOutlined } from "@ant-design/icons";

import { Button, Popover } from "antd";
import { useState } from "react";
import { useTranslation } from "react-i18next";

import { NoRequiredAuthorityTooltip } from "@/components";
import { useAuthority } from "@/hooks";
import { EAuthorities, type TScheduledTaskItem } from "@/models";

import { TriggerForm } from "./TriggerForm";

interface IProps {
    item: TScheduledTaskItem;
    onChange: () => Promise<void>;
}

export const EditTriggerPopover = ({ item, onChange }: IProps) => {
    const scheduledTasksAccess = useAuthority(EAuthorities.SCHEDULED_TASKS_MODIFY);

    const { t } = useTranslation();

    const [open, setOpen] = useState(false);

    return (
        <Popover
            open={open}
            onOpenChange={setOpen}
            trigger="click"
            placement="bottomRight"
            destroyOnHidden
            title={t("ScheduledTasks.editTrigger")}
            content={<TriggerForm item={item} onChange={onChange} onClose={() => setOpen(false)} />}
        >
            <NoRequiredAuthorityTooltip disabled={!scheduledTasksAccess}>
                <Button icon={<EditOutlined />} disabled={!scheduledTasksAccess}>
                    {t("ScheduledTasks.editTrigger")}
                </Button>
            </NoRequiredAuthorityTooltip>
        </Popover>
    );
};
