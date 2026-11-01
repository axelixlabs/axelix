
import { DynamicCodeBlock } from "fumadocs-ui/components/dynamic-codeblock";

import { getLatestVersion } from "@/lib/docker-version";

interface DockerImageCodeProps {
    major: number | string;
    language: "bash" | "yaml";
    code: string;
    title?: string;
}

export async function DockerImageCode({
                                          major,
                                          language,
                                          code,
                                          title,
                                      }: DockerImageCodeProps) {
    try {
        const majorVersion = Number(major);

        if (!Number.isInteger(majorVersion) || majorVersion < 1) {
            throw new Error("Major version must be a positive integer");
        }

        const version = await getLatestVersion(majorVersion);

        const resolvedCode = code.replaceAll(
            "AXELIX_VERSION_PLACEHOLDER",
            version,
        );

        return (
            <DynamicCodeBlock
                lang={language}
                code={resolvedCode}
                options={{
                    themes: {
                        light: "github-light",
                        dark: "github-dark",
                    },
                }}
                codeblock={{
                    title,
                    allowCopy: true,
                }}
            />
        );
    } catch (error) {
        console.error("Failed to render Axelix Docker code:", error);

        return (
            <pre>
                <code>Unable to load the latest Axelix image version.</code>
            </pre>
        );
    }
}
