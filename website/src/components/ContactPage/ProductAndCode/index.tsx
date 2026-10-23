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
import { GITHUB_ISSUES_URL, GITHUB_URL } from "../../../utils";
import { SectionLabel } from "../../SectionLabel";
import { ContactRows, IContactRow } from "../ContactRows";
import shared from "../shared.module.css";

import styles from "./styles.module.css";

const ROWS: IContactRow[] = [
    {
        label: "Bugs, ideas & questions",
        value: "GitHub issues",
        note: "Bug reports, feature requests and questions. Include versions and a reproducer if you can.",
        href: GITHUB_ISSUES_URL,
        external: true,
    },
    {
        label: "Security vulnerabilities",
        value: "Security policy",
        note: "Report privately, following SECURITY.md.",
        href: `${GITHUB_URL}/blob/master/SECURITY.md`,
        external: true,
    },
];

export const ProductAndCode = () => {
    return (
        <section className={shared.Section} id="project">
            <div className={`MainContainer ${shared.TwoColumns}`}>
                <div className={shared.Heading}>
                    <SectionLabel index="02" label="Product & code" />
                    <h2 className={shared.Title}>Bugs, ideas and security reports.</h2>
                    <p className={styles.Description}>
                        Never report a vulnerability in a public issue. Follow the private process in our security
                        policy.
                    </p>
                </div>
                <ContactRows rows={ROWS} />
            </div>
        </section>
    );
};
