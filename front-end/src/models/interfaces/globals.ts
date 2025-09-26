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

/**
 * A common reusable interface for describing objects that consist of key and value pair.
 */
export interface IKeyValuePair {
  key: string;
  value: string;
}
