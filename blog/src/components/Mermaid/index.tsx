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
import { renderMermaidSVG } from "beautiful-mermaid";
import { CodeBlock, Pre } from "fumadocs-ui/components/codeblock";

interface IProps {
    chart: string;
}

/**
 * Renders a ```mermaid block — emitted as `<Mermaid chart="…" />` by
 * `remarkMdxMermaid` (see source.config.ts) — to an inline SVG at build time.
 * Falls back to a plain code block if the diagram fails to parse.
 */
export const Mermaid = ({ chart }: IProps) => {
    try {
        const svg = renderMermaidSVG(chart, {
            bg: "var(--color-fd-background)",
            fg: "var(--color-fd-foreground)",
            transparent: true,
            interactive: false,
        });
        return (
            <div
                className="mermaid"
                style={{ width: "100%", overflow: "hidden" }}
                dangerouslySetInnerHTML={{ __html: svg }}
            />
        );
    } catch {
        return (
            <CodeBlock title="Mermaid">
                <Pre>{chart}</Pre>
            </CodeBlock>
        );
    }
};
