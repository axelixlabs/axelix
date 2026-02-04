/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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

export interface ISelectOptionData {
    /**
     * The select option value
     */
    value: string;

    /**
     * The select option label
     */
    label: string;
}
