import { Accordion } from "components/Accordion";
import { TooltipWithCopy } from "components/TooltipWithCopy";
import type { PropsWithChildren } from "react";

import type { ITableRow } from "models";

import { EmptyHandler } from "../EmptyHandler";

import { TablePropertyValue } from "./TablePropertyValue";
import styles from "./styles.module.css";

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
        <Accordion
            header={
                <div className={styles.AccordionHeader}>
                    <div>{headerName}</div>
                    {children}
                </div>
            }
            headerStyles={styles.HeaderStyles}
            contentStyles={styles.ContentStyles}
            accordionExpanded
        >
            <EmptyHandler isEmpty={!properties.length}>
                {properties.map(({ key, displayKey, displayValue, isPrimary }) => (
                    <div key={key} className="TableRow">
                        <div className="RowChunk">
                            <TooltipWithCopy text={displayKey} />
                        </div>
                        <div className="RowChunk">
                            <TablePropertyValue propertyName={key} propertyValue={displayValue} isPrimary={isPrimary} />
                        </div>
                    </div>
                ))}
            </EmptyHandler>
        </Accordion>
    );
};
