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
    );
};

export default GarbageCollector;
