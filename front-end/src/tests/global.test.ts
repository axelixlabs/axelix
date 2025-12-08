/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import { describe, expect, it } from "vitest";

import { canonicalize } from "helpers";

describe("Canonicalize", () => {
    it("Converts a string to lowercase", () => {
        expect(canonicalize("HELLO")).toBe("hello");
        expect(canonicalize("HellO")).toBe("hello");
        expect(canonicalize("ПрИвЕт")).toBe("привет");
    });

    it("Removes spaces and punctuation marks", () => {
        expect(canonicalize("Hello, World!")).toBe("helloworld");
        expect(canonicalize("Привет, мир!")).toBe("приветмир");
    });

    it("Keeps only letters and digits", () => {
        expect(canonicalize("?A1-B2_C3:")).toBe("a1b2c3");
        expect(canonicalize("123-456.789_10")).toBe("12345678910");
    });

    it("Correctly handles already normalized strings", () => {
        expect(canonicalize("abc123")).toBe("abc123");
    });
});
