export interface ICommonSliceState {
  /**
   * True if a login request is in progress
   */
  loading: boolean;
  /**
   * Error message if login failed, empty string otherwise
   * */
  error: string;
}
