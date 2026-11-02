import { execFileSync } from "node:child_process";

/** Write this anywhere in an article to get the latest released Axelix version, e.g. `ghcr.io/axelixlabs/axelix:AXELIX_VERSION_PLACEHOLDER`. */
export const PLACEHOLDER = "AXELIX_VERSION_PLACEHOLDER";

/** Used when no release tag can be read, e.g. a shallow clone or sources without `.git`. */
export const FALLBACK_VERSION = "1.2.2";

let latestVersion: string | undefined;

/**
 * The newest release tag (`vX.Y.Z`) in the local repository. Pre-release tags such as `v1.3.0-RC1` are skipped.
 * Read once per process — it is a local `git` call, so the MDX workers need no shared state.
 */
function getLatestVersion(): string {
    if (latestVersion) return latestVersion;

    try {
        const tags = execFileSync("git", ["tag", "--list", "v*", "--sort=-v:refname"], {
            encoding: "utf8",
            stdio: ["ignore", "pipe", "pipe"],
        });
        const latestTag = tags.split("\n").find((tag) => /^v\d+\.\d+\.\d+$/.test(tag.trim()));

        if (!latestTag) throw new Error("no vX.Y.Z release tags found");

        latestVersion = latestTag.trim().slice(1);
    } catch (error) {
        const reason = error instanceof Error ? error.message.trim() : String(error);

        console.warn(`Could not resolve the latest Axelix version from git tags, using ${FALLBACK_VERSION}: ${reason}`);

        latestVersion = FALLBACK_VERSION;
    }

    return latestVersion;
}

type Node = {
    value?: unknown;
    url?: unknown;
    attributes?: unknown;
    children?: Node[];
};

/** Replaces the placeholder in text, code, inline code, raw HTML, link/image URLs and MDX JSX attributes. */
export function replaceVersionPlaceholder(node: Node, version: string): void {
    if (typeof node.value === "string") node.value = node.value.replaceAll(PLACEHOLDER, version);
    if (typeof node.url === "string") node.url = node.url.replaceAll(PLACEHOLDER, version);

    // MDX JSX elements keep attributes as an array of `{ value }` nodes; directives (`:::note`) use a plain object.
    if (Array.isArray(node.attributes)) {
        for (const attribute of node.attributes as Node[]) replaceVersionPlaceholder(attribute, version);
    }

    for (const child of node.children ?? []) replaceVersionPlaceholder(child, version);
}

export default function remarkAxelixVersion() {
    return (tree: Node) => replaceVersionPlaceholder(tree, getLatestVersion());
}
