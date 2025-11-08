import type {
    IDetailsBuild,
    IDetailsGit,
    IDetailsOS,
    IDetailsRuntime,
    IDetailsSpring,
} from "models/interfaces/details";

export type DetailsBuildValuesData = IDetailsGit | IDetailsBuild | IDetailsSpring | IDetailsRuntime | IDetailsOS;
