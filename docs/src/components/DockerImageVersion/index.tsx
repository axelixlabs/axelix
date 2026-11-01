import { getLatestVersion } from "@/lib/docker-version";

interface DockerImageVersionProps {
    major: number;
}

export async function DockerImageVersion({ major }: DockerImageVersionProps) {
    try {
        return <code>{await getLatestVersion(major)}</code>;
    } catch (error) {
        console.error("Failed to fetch Axelix Docker image version:", error);

        return <code>{`${major}.x`}</code>;
    }
}
