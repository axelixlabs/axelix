export interface ILoginSubmitValue {
  username: string;
  password: string;
}

export interface ILoginSliceState {
  loading: boolean;
  accessToken: string | null;
  error: string;
}
