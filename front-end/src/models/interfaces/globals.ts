/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

    public static loading<U>(): StatefulRequest<U> {
        return new StatefulRequest<U>(true, "", null);
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

export interface IColorPallete {
    /**
     * The primary color
     */
    colorPrimary: string;

    /**
     * The color used when the element is hovered
     */
    colorPrimaryHover: string;

    /**
     * The color used when the element is active
     */
    colorPrimaryActive: string;
}
