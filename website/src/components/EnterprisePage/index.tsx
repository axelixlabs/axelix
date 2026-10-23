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
import { Reveal } from "../Reveal";

import { Editions } from "./Editions";
import { EnterpriseHero } from "./EnterpriseHero";
import { Includes } from "./Includes";
import { OpenCore } from "./OpenCore";
import { PilotCta } from "./PilotCta";

export const EnterprisePage = () => {
    return (
        <>
            <EnterpriseHero />
            <Reveal>
                <OpenCore />
            </Reveal>
            <Reveal>
                <Editions />
            </Reveal>
            <Reveal>
                <Includes />
            </Reveal>
            <Reveal>
                <PilotCta />
            </Reveal>
        </>
    );
};
