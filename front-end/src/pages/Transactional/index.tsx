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

import { EmptyHandler, Loader, PageSearch } from "components";
import { fetchData, filterTransactionalData } from "helpers";
import { type ITransactionalResponseData, StatefulRequest } from "models";
import { getTransactionalData } from "services";

import { TransactionalList } from "./TransactionalList";

const Transactional = () => {
    const [transactionalData, setTransactionalData] = useState(StatefulRequest.loading<ITransactionalResponseData[]>());
    const [search, setSearch] = useState<string>("");

    useEffect(() => {
        fetchData(setTransactionalData, () => getTransactionalData());
    }, []);

    if (transactionalData.loading) {
        return <Loader />;
    }

    if (transactionalData.error) {
        return <EmptyHandler isEmpty />;
    }

    const transactionalFeed = transactionalData.response!;
    const effectiveTransactionalData = search ? filterTransactionalData(transactionalFeed, search) : transactionalFeed;
    const addonAfter = `${effectiveTransactionalData.length} / ${transactionalFeed.length}`;

    return (
        <>
            <PageSearch setSearch={setSearch} addonAfter={addonAfter} />
            <TransactionalList effectiveTransactionalData={effectiveTransactionalData} />
        </>
    );
};

export default Transactional;
