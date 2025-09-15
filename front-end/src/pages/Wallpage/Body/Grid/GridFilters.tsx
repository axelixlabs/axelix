/**
 * An immutable filters for the main Grid
 */
export interface GridFilters {

    /**
     * The arbitraty query string
     */
    queryString?: string

    /**
     * Creates a new immutable copy of this GridFilters 
     * 
     * @param queryString 
     */
    copy(queryString: string) : GridFilters
}

/**
 * @returns returns an empty GridGilters 
 */
export function emptyFilter() : GridFilters {

    return {
        queryString: undefined,

        copy: function (newQueryString: string): GridFilters {
            return {
                queryString: newQueryString,
                copy: this.copy
            };
        }
    }
}