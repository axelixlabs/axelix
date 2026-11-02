import { describe, expect, it } from "vitest";

import { PLACEHOLDER, replaceVersionPlaceholder } from "../lib/remark-axelix-version";

describe("replaceVersionPlaceholder", () => {
    it("replaces the placeholder in every kind of node", () => {
        // given
        const tree = {
            children: [
                { value: `docker pull ghcr.io/axelixlabs/axelix:${PLACEHOLDER}` },
                { children: [{ value: `version ${PLACEHOLDER} and ${PLACEHOLDER}` }] },
                { url: `https://github.com/axelixlabs/axelix/releases/tag/v${PLACEHOLDER}`, children: [] },
                { attributes: [{ value: `axelix-${PLACEHOLDER}.jar` }, { value: { type: "expression" } }] },
            ],
        };

        // when
        replaceVersionPlaceholder(tree, "1.4.2");

        // then
        expect(tree).toEqual({
            children: [
                { value: "docker pull ghcr.io/axelixlabs/axelix:1.4.2" },
                { children: [{ value: "version 1.4.2 and 1.4.2" }] },
                { url: "https://github.com/axelixlabs/axelix/releases/tag/v1.4.2", children: [] },
                { attributes: [{ value: "axelix-1.4.2.jar" }, { value: { type: "expression" } }] },
            ],
        });
    });

    it("leaves text without the placeholder untouched", () => {
        // given
        const tree = { children: [{ value: "docker pull ghcr.io/axelixlabs/axelix:1.2.2" }] };

        // when
        replaceVersionPlaceholder(tree, "1.4.2");

        // then
        expect(tree).toEqual({ children: [{ value: "docker pull ghcr.io/axelixlabs/axelix:1.2.2" }] });
    });
});
