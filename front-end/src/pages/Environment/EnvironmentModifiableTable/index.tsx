import { Link, useParams } from "react-router-dom";

import { Accordion, Copy, EmptyHandler, TablePropertyValue } from "components";
import { normalizeHtmlElementId } from "helpers";
import type { IEnvironmentTableRow } from "models";

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
    properties: IEnvironmentTableRow[];
}

export const EnvironmentModifiableTable = ({ headerName, properties }: IProps) => {
    const { instanceId } = useParams();

    return (
        <div className={`AccordionsWrapper ${styles.AccordionWrapper}`}>
            <Accordion
                header={<div className={styles.AccordionHeader}>{headerName}</div>}
                headerStyles={styles.HeaderStyles}
                contentStyles={styles.ContentStyles}
                accordionExpanded
            >
                <EmptyHandler isEmpty={!properties.length}>
                    {properties.map(({ key, displayKey, displayValue, isPrimary, configPropsBeanName }) => (
                        <div key={key} className="TableRow">
                            <div className={`RowChunk ${styles.KeyChunk}`}>
                                {displayKey} <Copy text={displayKey} />
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
