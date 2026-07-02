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
import { Button } from "antd";
import { type Dispatch, type SetStateAction, useEffect, useId, useRef } from "react";
import { TimepickerUI } from "timepicker-ui";

import "timepicker-ui/main.css";

import { useTranslation } from "react-i18next";

import { getTimepickerClockConfig, timepickerDataConvertToSeconds } from "helpers";
import type { ITimepickerData, TChangeLoggerLevel } from "models";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Selected level
     */
    selectedLevel: string;

    /**
     * Controls the visibility of the parent popover.
     */
    setPopoverOpen: Dispatch<SetStateAction<boolean>>;

    /**
     * Callback to change the logger level.
     */
    handleChange: TChangeLoggerLevel;
}

const { locale, type } = getTimepickerClockConfig();

export const LoggerTimePicker = ({ selectedLevel, setPopoverOpen, handleChange }: IProps) => {
    const inputRef = useRef<HTMLInputElement>(null);
    const timepickerRef = useRef<TimepickerUI>(null);
    const containerId = useId();
    const { t } = useTranslation();

    const clickHandler = (): void => {
        const data: ITimepickerData | undefined = timepickerRef.current?.getValue();
        const ttlSeconds = timepickerDataConvertToSeconds(data);

        if (data) {
            handleChange(selectedLevel, ttlSeconds);
            setPopoverOpen(false);
        }
    };

    useEffect(() => {
        if (!inputRef.current) {
            return;
        }

        const timepicker = new TimepickerUI(inputRef.current, {
            ui: {
                inline: {
                    enabled: true,
                    containerId: containerId,
                    autoUpdate: true,
                    showButtons: false,
                },
                cssClass: styles.Timepicker,
            },
            labels: {
                time: "",
            },
            clock: {
                type: type,
                currentTime: {
                    updateInput: true,
                    locales: locale,
                    time: new Date(),
                },
            },
        });

        timepicker.create();
        timepickerRef.current = timepicker;

        return () => {
            timepicker.destroy();
        };
    }, []);

    return (
        <>
            <div id={containerId} className={styles.TimePickerWrapper}>
                <input ref={inputRef} className="VisuallyHidden" />
            </div>

            <div className={styles.ActionButtons}>
                <Button block onClick={() => setPopoverOpen(false)}>
                    {t("cancel")}
                </Button>

                <Button type="primary" block onClick={clickHandler}>
                    {t("confirm")}
                </Button>
            </div>
        </>
    );
};
