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
import assert from "node:assert/strict";
import { readFileSync } from "node:fs";
import test from "node:test";

const output = new URL("../../.next/server/app/", import.meta.url);
const readOutput = (name) => readFileSync(new URL(name, output), "utf8");

test("LLM index links to the prerendered Markdown and the other Axelix apps", () => {
    const index = readOutput("llms.txt.body");

    for (const path of ["/index.md", "/llms-full.txt", "/docs/llms.txt", "/blog/llms.txt"]) {
        assert.ok(index.includes(`https://axelix.io${path}`), `missing ${path}`);
    }
});

test("full export embeds the same homepage Markdown", () => {
    assert.ok(readOutput("llms-full.txt.body").includes(readOutput("index.md.body")));
});

test("the prerendered routes return the intended content types", () => {
    for (const [route, type] of [
        ["llms.txt", "text/plain"],
        ["llms-full.txt", "text/plain"],
        ["index.md", "text/markdown"],
    ]) {
        const { status, headers } = JSON.parse(readOutput(`${route}.meta`));
        assert.equal(status, 200, `${route} should be available`);
        assert.equal(headers["content-type"], `${type}; charset=utf-8`);
    }
});

test("Markdown includes the sections and interactive content visible in the HTML", () => {
    const html = readOutput("index.html");
    const markdown = readOutput("index.md.body");
    const headline = markdown.match(/^# (.*?) \(https:\/\//)?.[1];
    const intro = markdown.split("\n\n")[1];
    const visibleText = html.replace(/<[^>]*>/g, "").replace(/\s+/g, " ");

    assert.ok(headline && visibleText.includes(headline), "the homepage headline and Markdown have diverged");
    assert.ok(intro && visibleText.includes(intro), "the homepage introduction and Markdown have diverged");
    assert.ok(readOutput("llms.txt.body").includes(intro), "the index and homepage introduction have diverged");

    const titles = [
        ...markdown.matchAll(/^### (.+)$/gm),
        ...markdown.matchAll(/^- \[\*\*(.+?)\*\*\]\(https:\/\/axelix\.io\/docs\//gm),
        ...markdown.matchAll(/^- \*\*(.+?)\*\*: /gm),
    ].map((match) => match[1]);
    assert.ok(titles.length >= 25, "expected FAQ, example, capability, metric and Enterprise entries");

    for (const title of titles) {
        assert.ok(html.includes(title.replaceAll("&", "&amp;")), `the HTML no longer includes ${title}`);
        assert.ok(markdown.includes(title), `the Markdown no longer includes ${title}`);
    }

    for (const method of ["Docker Compose", "Kubernetes", "Bare Metal / VMs"]) {
        assert.ok(markdown.includes(`**${method}**`), `missing installation method ${method}`);
    }
});
