export interface ApplicationInstance {

    /**
     * The name of the application
     */
    name: string;

    /**
     * Short commit hash version 
     */
    commitHash: string;

    /**
     * Version of the spring boot being used
     */
    springBootVersion: string;

    /**
     * version of the artifact deployed
     */
    version: string;

    /**
     * the time when the app was actually deployed
     */
    deployedAt: Date;

    /**
     * Tells if the application is not healthy
     */
    state: InstanceState;
}

export const enum InstanceState {
    UP,
    DOWN,
    UNKNOWN
}