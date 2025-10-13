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


export interface ITableRow {
  /**
   * The technical identifier of the key inside the table.
   *
   * As this component is initially intended to be used by the {@link Environment} and {@link ConfigProps} components,
   * the 'key' is the full name of the property
   */
  key: string;

  /**
   * The value of the property as it should be displayed
   */
  displayKey: string;

  /**
   * The value to be displayed
   */
  displayValue: string;
}

