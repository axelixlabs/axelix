import { TooltipWithCopy } from "components/TooltipWithCopy";
import type { PropsWithChildren } from "react";

import type { ITableRow } from "models";

import { EmptyHandler } from "../EmptyHandler";

import { TablePropertyValue } from "./TablePropertyValue";

interface IProps {
    /**
     * Table header name
     */
    headerName: string;

    /**
     * Table rows data
     */
    properties: ITableRow[];
}

export const ModifiableTableSection = ({ headerName, properties, children }: PropsWithChildren<IProps>) => {
    return (
        // TODO: this css class CustomizedAntdTable is used also for scheduled tasks, and I do not think it is correct
        <div className="CustomizedAntdTable">
            <div className="TableHeader">
                <div className="RowChunk">
                    <div>{headerName}</div>
                    {children}
                </div>
            </div>

            <EmptyHandler isEmpty={!properties.length}>
                {properties.map(({ key, displayKey, displayValue }) => (
                    <div key={key} className="TableRow">
                        <div className="RowChunk">
                            <TooltipWithCopy text={displayKey} />
                        </div>
                        <div className="RowChunk">
                            <TablePropertyValue propertyName={key} propertyValue={displayValue} />
                        </div>
                    </div>
                ))}
            </EmptyHandler>
        </div>
    );
};
