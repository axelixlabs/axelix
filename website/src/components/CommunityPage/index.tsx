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

import { CommunityHero } from "./CommunityHero";
import { Contribute } from "./Contribute";
import { FollowAlong } from "./FollowAlong";
import { LatestPosts } from "./LatestPosts";
import { Partners } from "./Partners";
import { Rules } from "./Rules";
import { Talks } from "./Talks";

export const CommunityPage = () => {
    return (
        <>
            <CommunityHero />
            <Reveal early>
                <FollowAlong />
            </Reveal>
            <Reveal early>
                <Partners />
            </Reveal>
            <Reveal early>
                <LatestPosts />
            </Reveal>
            <Reveal early>
                <Contribute />
            </Reveal>
            <Reveal early>
                <Talks />
            </Reveal>
            <Reveal early>
                <Rules />
            </Reveal>
        </>
    );
};
