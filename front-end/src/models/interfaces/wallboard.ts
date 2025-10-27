export interface IInstanceCard {
    instanceId: string;
    springBootVersion: string;
    javaVersion: string;
    status: string;
    name: string;
    serviceVersion: string;
    commitShaShort: string;
    deployedFor: string;
}

export interface IServiceCardsResponseBody {
    instances: IInstanceCard[];
}
