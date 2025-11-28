/**
 * @deprecated We're attempting to migrate from this abstraction, since redux slice in and
 * of itself does not have properties {@link loading} or {@link error}. These are the properties
 * of the individual request, and not the slice. Use {@link IRequest} instead.
 */
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
 * The {@link IRequest} implementation that is stateful, meaning, that it produces
 * some result/state, see the `response` field.
 */
export class StatefulRequest<T> implements IRequest {
    constructor(
        public loading: boolean,
        public error: string,
        public response: T | null,
    ) {}

    public static loading<U>(state: boolean = true): StatefulRequest<U> {
        return new StatefulRequest<U>(state, "", null);
    }

    public static success<U>(response: U): StatefulRequest<U> {
        return new StatefulRequest<U>(false, "", response);
    }

    public static error<U>(error: string): StatefulRequest<U> {
        return new StatefulRequest<U>(false, error, null);
    }
}

/**
 * The {@link IRequest} implementation that does not evaluate to any value
 */
export class StatelessRequest implements IRequest {
    constructor(
        public loading: boolean,
        public error: string,
        public completed: boolean,
    ) {}

    public completedSuccessfully(): boolean {
        return this.completed && !this.error && !this.loading;
    }

    public static loading(): StatelessRequest {
        return new StatelessRequest(true, "", false);
    }

    public static inactive(): StatelessRequest {
        return new StatelessRequest(false, "", false);
    }

    public static success(): StatelessRequest {
        return new StatelessRequest(false, "", true);
    }

    public static error(error: string): StatelessRequest {
        return new StatelessRequest(false, error, true);
    }
}

export interface IRequest {
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

export interface IUpdatePropertyRequestData {
    /**
     * Instance id of service
     */
    instanceId: string;
    /**
     * Property name
     */
    propertyName: string;
    /**
     * New property value
     */
    newValue: string;
}

export interface IErrorResponse {
    /**
     * Server text used to display a user-friendly notification
     */
    code?: string;
}
