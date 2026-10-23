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
import { CONTACT_EMAIL, ENTERPRISE_EMAIL } from "../../../utils";
import { SectionLabel } from "../../SectionLabel";
import { ContactRows, IContactRow } from "../ContactRows";
import shared from "../shared.module.css";

const ROWS: IContactRow[] = [
    {
        label: "General",
        value: CONTACT_EMAIL,
        note: "Questions, feedback, press, talks and partnerships.",
        href: `mailto:${CONTACT_EMAIL}`,
    },
    {
        label: "Enterprise & pilots",
        value: ENTERPRISE_EMAIL,
        note: "Pricing, contracts and joining the Enterprise pilot.",
        href: `mailto:${ENTERPRISE_EMAIL}`,
    },
];

export const WriteToUs = () => {
    return (
        <section className={`${shared.Section} ${shared.PaperSection}`} id="write">
            <div className={`MainContainer ${shared.TwoColumns}`}>
                <div className={shared.Heading}>
                    <SectionLabel index="01" label="Write to us" />
                    <h2 className={shared.Title}>Email the team directly.</h2>
                </div>
                <ContactRows rows={ROWS} />
            </div>
        </section>
    );
};
