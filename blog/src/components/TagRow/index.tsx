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
import { PlainTag } from "../PlainTag";

interface IProps {
    tags: string[];
    max?: number;
}

/** Up to `max` tags, with a `+N` overflow chip. */
export const TagRow = ({ tags, max = 3 }: IProps) => {
    const shown = tags.slice(0, Math.max(0, max));
    const overflow = tags.length - shown.length;
    return (
        <div className="rtags">
            {shown.map((t) => (
                <PlainTag key={t} label={t} />
            ))}
            {overflow > 0 && <span className="tag tag-more">+{overflow}</span>}
        </div>
    );
};
