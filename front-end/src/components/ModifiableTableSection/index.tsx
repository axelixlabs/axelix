import { Accordion } from "components/Accordion";
import { TooltipWithCopy } from "components/TooltipWithCopy";
import type { PropsWithChildren } from "react";
import { Link, useParams } from "react-router-dom";

import { normalizeHtmlElementId } from "helpers";
import type { ITableRow } from "models";

import { EmptyHandler } from "../EmptyHandler";

import { TablePropertyValue } from "./TablePropertyValue";
import styles from "./styles.module.css";

import LinkIcon from "assets/icons/link.svg";

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
    const { instanceId } = useParams();

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
                    {properties.map(({ key, displayKey, displayValue, isPrimary, configPropsBeanName }) => (
                        <div key={key} className="TableRow">
                            <div className={`RowChunk ${styles.KeyChunk}`}>
                                <TooltipWithCopy text={displayKey} />
                                {configPropsBeanName && (
                                    <Link
                                        to={`/instance/${instanceId}/config-props#${normalizeHtmlElementId(configPropsBeanName)}`}
                                        onClick={(e) => e.stopPropagation()}
                                    >
                                        <img src={LinkIcon} alt="Link icon" />
                                    </Link>
                                )}
                            </div>
                            <div className={`RowChunk ${styles.ValueChunk}`}>
                                <TablePropertyValue
                                    propertyName={key}
                                    propertyValue={displayValue}
                                    isPrimary={isPrimary}
                                />
                            </div>
                        </div>
                    ))}
                </EmptyHandler>
            </Accordion>
        </div>
    );
};
