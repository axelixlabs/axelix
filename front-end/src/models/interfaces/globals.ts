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

export interface IKeyValuePair {
  /**
   * A common reusable interface for describing objects that contain key and value properties.
   * */
  key: string;
  value: string;
}
