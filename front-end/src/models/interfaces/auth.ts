export interface ILoginSubmitValue {
  username: string;
  password: string;
}

export interface ILoginThunkInitialState {
  loading: boolean;
  accessToken: string | null;
  error: string;
}
