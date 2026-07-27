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
import Link from "next/link";

export default function NotFound() {
    return (
        <div className="page">
            <div className="wrap" style={{ padding: "120px 0", textAlign: "center" }}>
                <h1
                    style={{
                        fontSize: "clamp(40px, 6vw, 80px)",
                        fontWeight: 500,
                        margin: 0,
                    }}
                >
                    404
                </h1>
                <p style={{ color: "var(--ink-3)", marginTop: 12 }}>That page wandered off. Let&apos;s get you back.</p>
                <p style={{ marginTop: 24 }}>
                    <Link className="ext-link" href="/">
                        ← Back to the blog
                    </Link>
                </p>
            </div>
        </div>
    );
}
