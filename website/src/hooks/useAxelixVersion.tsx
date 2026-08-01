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
"use client";
import { IAxelixVersionData, IGithubReleaseResponseBody } from "@/models";

import { ReactNode, createContext, useContext, useEffect, useState } from "react";

const AxelixVersionContext = createContext<IAxelixVersionData>({
    version: null,
    loading: true,
});

/**
 * Fetches the current Axelix version once (from the latest GitHub release) and
 * shares it with every consumer via {@link useAxelixVersion}. All Axelix
 * artifacts ship under the same version, so a single fetch serves the whole page.
 *
 * The GitHub release tag carries a leading `v` (e.g. `v1.0.0`) that is stripped
 * here, since the published Docker image tag and the Maven/Gradle/Helm
 * coordinates are all versioned without the prefix.
 */
export const AxelixVersionProvider = ({ children }: { children: ReactNode }) => {
    const [axelixVersionData, setAxelixVersionData] = useState<IAxelixVersionData>({
        version: null,
        loading: true,
    });

    useEffect(() => {
        async function fetchAxelixVersion() {
            try {
                const response = await fetch("https://api.github.com/repos/axelixlabs/axelix/releases/latest");

                if (!response.ok) {
                    throw new Error();
                }

                const data: IGithubReleaseResponseBody = await response.json();

                setAxelixVersionData((prev) => ({
                    ...prev,
                    version: data.tag_name.replace(/^v/, ""),
                }));
            } catch {
                setAxelixVersionData((prev) => ({
                    ...prev,
                    version: "1.0.0",
                }));
            } finally {
                setAxelixVersionData((prev) => ({
                    ...prev,
                    loading: false,
                }));
            }
        }

        fetchAxelixVersion();
    }, []);

    return <AxelixVersionContext.Provider value={axelixVersionData}>{children}</AxelixVersionContext.Provider>;
};

export const useAxelixVersion = () => useContext(AxelixVersionContext);
