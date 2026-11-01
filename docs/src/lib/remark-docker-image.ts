import type { Code, Root } from "mdast";
import { visit } from "unist-util-visit";

const DOCKER_IMAGE_PATTERN = /(?:ghcr\.io|docker\.io)\/axelixlabs\/axelix/;

const PLACEHOLDER = "AXELIX_VERSION_PLACEHOLDER";

const UNTAGGED_IMAGE_PATTERN = /((?:ghcr\.io|docker\.io)\/axelixlabs\/axelix)(?![:@\w.-])/g;

function getSupportedLanguage(language: string | null | undefined): "bash" | "yaml" {
    return language === "yaml" || language === "yml" ? "yaml" : "bash";
}

export default function remarkDockerImage() {
    return (tree: Root) => {
        visit(tree, "code", (node: Code) => {
            let code = node.value;

            if (!DOCKER_IMAGE_PATTERN.test(code)) {
                return;
            }

            // Avoid processing examples that already use the placeholder.
            if (code.includes(PLACEHOLDER)) {
                return;
            }

            // Add a dynamic version to every untagged Axelix image.
            code = code.replace(UNTAGGED_IMAGE_PATTERN, `$1:${PLACEHOLDER}`);

            if (code === node.value) {
                return;
            }

            const component = {
                type: "mdxJsxFlowElement" as const,
                name: "DockerImageCode",
                attributes: [
                    {
                        type: "mdxJsxAttribute" as const,
                        name: "major",
                        value: "1",
                    },
                    {
                        type: "mdxJsxAttribute" as const,
                        name: "language",
                        value: getSupportedLanguage(node.lang),
                    },
                    {
                        type: "mdxJsxAttribute" as const,
                        name: "code",
                        value: code,
                    },
                ],
                children: [],
            };

            Object.assign(node, component);
        });
    };
}
