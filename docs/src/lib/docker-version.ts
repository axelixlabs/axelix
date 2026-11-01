interface GitHubRelease {
    tag_name: string;
    draft: boolean;
    prerelease: boolean;
}

const versionPromises = new Map<number, Promise<string>>();

async function fetchLatestVersion(major: number): Promise<string> {
    const releases: GitHubRelease[] = [];
    let page = 1;

    while (true) {
        const response = await fetch(
            `https://api.github.com/repos/axelixlabs/axelix/releases?per_page=100&page=${page}`,
            {
                headers: {
                    Accept: "application/vnd.github+json",
                },
                next: {
                    revalidate: 3600,
                },
            },
        );

        if (!response.ok) {
            throw new Error(`GitHub Releases API returned ${response.status}`);
        }

        const pageReleases: GitHubRelease[] = await response.json();

        releases.push(...pageReleases);

        if (pageReleases.length < 100) {
            break;
        }

        page++;
    }

    const versions = releases
        .filter((release) => !release.draft && !release.prerelease)
        .map((release) => release.tag_name.replace(/^v/, ""))
        .filter((version) => new RegExp(`^${major}\\.\\d+\\.\\d+$`).test(version));

    versions.sort((a, b) => {
        const left = a.split(".").map(Number);
        const right = b.split(".").map(Number);

        for (let i = 0; i < 3; i++) {
            if (left[i] !== right[i]) {
                return right[i] - left[i];
            }
        }

        return 0;
    });

    if (!versions.length) {
        throw new Error(`No stable release found for major version ${major}`);
    }

    return versions[0];
}

export function getLatestVersion(major: number): Promise<string> {
    if (!Number.isInteger(major) || major < 1) {
        throw new Error("Major version must be a positive integer");
    }

    const cachedPromise = versionPromises.get(major);

    if (cachedPromise) {
        return cachedPromise;
    }

    const promise = fetchLatestVersion(major);

    versionPromises.set(major, promise);

    promise.catch(() => {
        if (versionPromises.get(major) === promise) {
            versionPromises.delete(major);
        }
    });

    return promise;
}
