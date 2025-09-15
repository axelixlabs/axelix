import { InstanceState, type ApplicationInstance } from "../types/ApplicationInstance"

export async function getAllInstances() : Promise<Array<ApplicationInstance>> {
    
    return Promise.resolve(
        [
            {
                name: "doc-copy-request",
                commitHash: "a1b2c3d",
                version: "1.0.0-SNAPSHOT",
                springBootVersion: "2.3.8",
                deployedAt : new Date(),
                state: InstanceState.UP,
            },
            {
                name: "deals-calendar-bank",
                commitHash: "k12e02l",
                version: "3.2.1",
                springBootVersion: "4.0.0-M1",
                deployedAt : new Date(),
                state: InstanceState.UP,
            },
            {
                name: "ms-product-offer-stub",
                commitHash: "fewm22p",
                version: "0.2.1-alpha",
                springBootVersion: "1.2.6.RELEASE",
                deployedAt : new Date(),
                state: InstanceState.DOWN,
            },
            {
                name: "ms-print-processing-service",
                commitHash: "lq24k12",
                version: "1.4.1",
                springBootVersion: "3.1.13",
                deployedAt : new Date(),
                state: InstanceState.UNKNOWN
            },
            {
                name: "ms-ods",
                commitHash: "j29jk12",
                version: "2.5.3",
                springBootVersion: "2.0.0.RELEASE",
                deployedAt : new Date(),
                state: InstanceState.UP
            },
            {
                name: "ms-client-dictionary",
                commitHash: "kqwl2o1",
                version: "2.1.1",
                springBootVersion: "3.1.1",
                deployedAt : new Date(),
                state: InstanceState.DOWN
            },
            {
                name: "ms-client-dictionary-5",
                commitHash: "kqwl2o1",
                version: "2.1.1",
                springBootVersion: "3.1.1",
                deployedAt : new Date(),
                state: InstanceState.DOWN
            }
,            {
                name: "ms-client-dictionary-4",
                commitHash: "kqwl2o1",
                version: "2.1.1",
                springBootVersion: "3.1.1",
                deployedAt : new Date(),
                state: InstanceState.DOWN
            }
,            {
                name: "ms-client-dictionary-3",
                commitHash: "kqwl2o1",
                version: "2.1.1",
                springBootVersion: "3.1.1",
                deployedAt : new Date(),
                state: InstanceState.DOWN
            }
,            {
                name: "ms-client-dictionary-2",
                commitHash: "kqwl2o1",
                version: "2.1.1",
                springBootVersion: "3.1.1",
                deployedAt : new Date(),
                state: InstanceState.DOWN
            }
,            {
                name: "ms-client-dictionary-1",
                commitHash: "kqwl2o1",
                version: "2.1.1",
                springBootVersion: "3.1.1",
                deployedAt : new Date(),
                state: InstanceState.DOWN
            }

        ]
    )

    // const response : Response = await fetch('https://server.url/applications/grid')

    // if (!response.ok) {
    //     throw new Error("Cannot reach the backend to fetch the services grid") 
    // }

    // const data: Array<ApplicationInstance> = await response.json()

    // return data
}