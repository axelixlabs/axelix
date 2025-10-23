import type { ICommonSliceState } from "./globals";

export interface ILoginSubmitValue {
    /**
     * The username of the user.
     * Used to identify the user during the login process.
     */
    username: string;
    /**
     * The password of the user.
     * Used to authenticate the user during the login process.
     */
    password: string;
}

export interface ILoginSliceState extends ICommonSliceState {
    /** Access token if login succeeded, null otherwise */
    accessToken: string | null;
}
