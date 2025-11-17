import { Accordion } from "components/Accordion";
import { TooltipWithCopy } from "components/TooltipWithCopy";
import type { PropsWithChildren } from "react";

import { EmptyHandler, TablePropertyValue } from "components";
import { normalizeHtmlElementId } from "helpers";
import type { ITableRow } from "models";

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

export const ConfigPropsModifiableTable = ({ headerName, properties, children }: PropsWithChildren<IProps>) => {
    return (
        <div className={`AccordionsWrapper ${styles.AccordionWrapper}`} id={normalizeHtmlElementId(headerName)}>
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
                    {properties.map(({ key, displayKey, displayValue }) => (
                        <div key={key} className="TableRow">
                            <div className="RowChunk">
                                <TooltipWithCopy text={displayKey} />
                            </div>
                            <div className={`RowChunk ${styles.ValueChunk}`}>
                                <TablePropertyValue propertyName={key} propertyValue={displayValue} />
                            </div>
                        </div>
                    ))}
                </EmptyHandler>
            </Accordion>
        </div>
    );
};
