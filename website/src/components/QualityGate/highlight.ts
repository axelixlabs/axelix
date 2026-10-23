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
import { TLanguage } from "./items";

const COLORS = {
    base: "#d8e3cf",
    keyword: "#8fb3e0",
    annotation: "#8fbd4b",
    string: "#b6d684",
    type: "#e6d49a",
    comment: "#6f786f",
    key: "#9cc3e0",
    value: "#e6d49a",
    number: "#f0a77a",
};

export interface ISegment {
    text: string;
    color: string;
}

const highlightXml = (line: string): ISegment[] => {
    const segments: ISegment[] = [];
    const pattern = /(<!--.*?-->)|(<\/?[\w.-]+>)/g;
    let last = 0;
    let match;

    while ((match = pattern.exec(line))) {
        if (match.index > last) {
            const text = line.slice(last, match.index);
            segments.push({ text, color: text.trim() ? COLORS.value : COLORS.base });
        }
        segments.push({ text: match[0], color: match[1] ? COLORS.comment : COLORS.key });
        last = match.index + match[0].length;
    }

    if (last < line.length) {
        segments.push({ text: line.slice(last), color: COLORS.value });
    }

    return segments;
};

const highlightYaml = (line: string): ISegment[] => {
    const comment = line.match(/^(\s*)(#.*)$/);
    if (comment) {
        return [
            { text: comment[1], color: COLORS.base },
            { text: comment[2], color: COLORS.comment },
        ];
    }

    const entry = line.match(/^(\s*)([\w.-]+)(:)(\s*)(.*)$/);
    if (!entry) {
        return [{ text: line, color: COLORS.base }];
    }

    const value = entry[5];
    let valueColor = COLORS.value;
    if (/^".*"$/.test(value)) {
        valueColor = COLORS.string;
    } else if (/^(true|false|always|\d+)$/.test(value)) {
        valueColor = COLORS.number;
    }

    return [
        { text: entry[1], color: COLORS.base },
        { text: entry[2], color: COLORS.key },
        { text: entry[3] + entry[4], color: COLORS.base },
        { text: value, color: valueColor },
    ];
};

const getJavaTokenColor = (match: RegExpExecArray) => {
    if (match[1]) {
        return COLORS.comment;
    }
    if (match[2]) {
        return COLORS.annotation;
    }
    if (match[3]) {
        return COLORS.string;
    }
    return match[4] ? COLORS.keyword : COLORS.type;
};

const highlightJava = (line: string): ISegment[] => {
    const segments: ISegment[] = [];
    const pattern = /(\/\/.*$)|(@\w+)|("[^"]*")|\b(public|private|class|new|return|final|static)\b|\b([A-Z]\w*)\b/g;
    let last = 0;
    let match;

    while ((match = pattern.exec(line))) {
        if (match.index > last) {
            segments.push({ text: line.slice(last, match.index), color: COLORS.base });
        }
        segments.push({ text: match[0], color: getJavaTokenColor(match) });
        last = match.index + match[0].length;
    }

    if (last < line.length) {
        segments.push({ text: line.slice(last), color: COLORS.base });
    }

    return segments;
};

export const highlight = (language: TLanguage, line: string): ISegment[] => {
    if (!line) {
        return [{ text: " ", color: COLORS.base }];
    }
    if (language === "xml") {
        return highlightXml(line);
    }
    return language === "yaml" ? highlightYaml(line) : highlightJava(line);
};
