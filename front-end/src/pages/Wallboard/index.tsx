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

import { EmptyHandler, Loader, PageSearch } from "components";
import { fetchData, filterInstances } from "helpers";
import { type IServiceCardsResponseBody, StatefulRequest } from "models";
import { getWallboardData } from "services";

import { WallboardCard } from "./WallboardCard";
import styles from "./styles.module.css";

export const Wallboard = () => {
    const [search, setSearch] = useState<string>("");
    const [wallboard, setWallboard] = useState(StatefulRequest.loading<IServiceCardsResponseBody>());

    useEffect(() => {
        fetchData(setWallboard, () => getWallboardData());
    }, []);

    if (wallboard.loading) {
        return <Loader />;
    }

    if (wallboard.error) {
        return <EmptyHandler isEmpty />;
    }

    const instanceCards = wallboard.response!.instances;
    const effectiveInstanceCards = search ? filterInstances(instanceCards, search) : instanceCards;

    const addonAfter = `${effectiveInstanceCards.length} / ${instanceCards.length}`;

    return (
        <>
            <PageSearch addonAfter={addonAfter} setSearch={setSearch} />

            <EmptyHandler isEmpty={instanceCards.length === 0}>
                <div className={styles.CardsResponsiveWrapper}>
                    {effectiveInstanceCards.map((data) => (
                        <WallboardCard data={data} key={data.instanceId} />
                    ))}
                </div>
            </EmptyHandler>
        </>
    );
};

export default Wallboard;
