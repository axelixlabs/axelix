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
import { EInstanceStatus, type TInstanceOption } from "@/models";

// TODO: Replace these mock rows with real instances belonging to the current application
// (groupId + artifactId) once the backend exposes application-scoped instance data.
export const DEMO_INSTANCES: TInstanceOption[] = [
    {
        instanceId: "demo-0",
        name: "spring-petclinic-maven-sb-4-5ddf9947b8-v2xjp",
        commitShaShort: "daeae48",
        deployedFor: "22h 24m",
        status: EInstanceStatus.UP,
    },
    {
        instanceId: "demo-1",
        name: "spring-petclinic-maven-sb-2-5f68f4c9b5-zhdsc",
        commitShaShort: "daeae48",
        deployedFor: "22h 27m",
        status: EInstanceStatus.UP,
    },
    {
        instanceId: "demo-2",
        name: "spring-petclinic-maven-sb-4-5ddf9947b8-k9wrt",
        commitShaShort: "a92f7b1",
        deployedFor: "18h 42m",
        status: EInstanceStatus.UP,
    },
    {
        instanceId: "demo-3",
        name: "spring-petclinic-maven-sb-2-5f68f4c9b5-p8mkn",
        commitShaShort: "a92f7b1",
        deployedFor: "6h 12m",
        status: EInstanceStatus.UP,
    },
    {
        instanceId: "demo-4",
        name: "spring-petclinic-maven-sb-4-5ddf9947b8-q4dhs",
        commitShaShort: "8c1d43e",
        deployedFor: "3h 05m",
        status: EInstanceStatus.UP,
    },
    {
        instanceId: "demo-5",
        name: "spring-petclinic-maven-sb-2-5f68f4c9b5-r7bvc",
        commitShaShort: "8c1d43e",
        deployedFor: "1h 48m",
        status: EInstanceStatus.UP,
    },
    {
        instanceId: "demo-6",
        name: "spring-petclinic-maven-sb-4-5ddf9947b8-n3fzq",
        commitShaShort: "daeae48",
        deployedFor: "54m",
        status: EInstanceStatus.UP,
    },
    {
        instanceId: "demo-7",
        name: "spring-petclinic-maven-sb-2-5f68f4c9b5-t6pld",
        commitShaShort: "daeae48",
        deployedFor: "—",
        status: EInstanceStatus.DOWN,
    },
    {
        instanceId: "demo-8",
        name: "spring-petclinic-maven-sb-4-5ddf9947b8-w2kcx",
        commitShaShort: "a92f7b1",
        deployedFor: "—",
        status: EInstanceStatus.UNKNOWN,
    },
    {
        instanceId: "demo-9",
        name: "spring-petclinic-maven-sb-2-5f68f4c9b5-b5gyn",
        commitShaShort: "8c1d43e",
        deployedFor: "12m",
        status: EInstanceStatus.UP,
    },
];
