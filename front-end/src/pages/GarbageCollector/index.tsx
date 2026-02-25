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
import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";

import { EmptyHandler, Loader } from "components";
import { fetchData } from "helpers";
import { type IGCLoggingStatusResponseBody, StatefulRequest } from "models";
import { getGCLoggingStatus } from "services";

import { GCDisabledMessage } from "./GCDisabled";
import { GCFirstSection } from "./GCFirstSection";
import { GCLogFeed } from "./GCLogFeed";
import styles from "./styles.module.css";

const GarbageCollector = () => {
    const { instanceId } = useParams();
    const [loggingStatusData, setLoggingStatusData] = useState(StatefulRequest.loading<IGCLoggingStatusResponseBody>());

    const loadGCStatus = () => {
        setLoggingStatusData(StatefulRequest.loading<IGCLoggingStatusResponseBody>());
        fetchData(setLoggingStatusData, () => getGCLoggingStatus(instanceId!));
    };

    useEffect(() => {
        loadGCStatus();
    }, []);

    if (loggingStatusData.loading) {
        return <Loader />;
    }

    if (loggingStatusData.error) {
        return <EmptyHandler isEmpty />;
    }

    const gcStatus = loggingStatusData.response!;
    const isLoggingStatusEnabled = gcStatus.enabled;

    return (
        <>
            <div className={styles.MainWrapper}>
                <GCFirstSection isLoggingStatusEnabled={isLoggingStatusEnabled} loadGCStatus={loadGCStatus} />

                <div className={styles.ContentWrapper}>
                    {isLoggingStatusEnabled ? (
                        <GCLogFeed />
                    ) : (
                        <GCDisabledMessage loggingStatusData={gcStatus} loadGCStatus={loadGCStatus} />
                    )}
                </div>
            </div>
        </>
    );
};

export default GarbageCollector;
