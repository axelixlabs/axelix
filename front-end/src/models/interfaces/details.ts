interface IDetailsGit {
    commitShaShort: string;
    branch: string;
    authorName: string;
    authorEmail: string;
    commitTimestamp: string;
}

export interface IDetailsRuntime {
    javaVersion: string;
    jdkVendor: string;
    garbageCollector: string;
    kotlinVersion: string;
}

interface IDetailsSpring {
    springBootVersion: string;
    SpringFrameworkVersion: string;
    SpringCloudVersion: string;
}

interface IDetailsBuild {
    artifact: string;
    version: string;
    group: string;
    time: string;
}

interface IDetailsOS {
    name: string;
    version: string;
    arch: string;
}

export interface IDetailsResponseBody {
    serviceName: string;
    git: IDetailsGit;
    runtime: IDetailsRuntime;
    spring: IDetailsSpring;
    build: IDetailsBuild;
    os: IDetailsOS;
}
