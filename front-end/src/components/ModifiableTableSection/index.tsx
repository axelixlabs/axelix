import type { PropsWithChildren } from 'react';

import { EmptyHandler, TooltipWithCopy } from 'components';
import { TablePropertyValue } from './TablePropertyValue';
import type { ITableRow } from 'models';

import styles from './styles.module.css'

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
        <div className={styles.Table}>
            <div className={styles.TableHeader}>
                <div className={styles.TableHeaderCell}>
                    <div>{headerName}</div>
                    {children}
                </div>
            </div>

            <EmptyHandler isEmpty={!properties.length}>
                {properties.map(({ key, displayKey, displayValue }) => (
                    <div
                        key={key}
                        className={styles.TableRow}
                    >
                        <div className={styles.RowChunk}>
                            <TooltipWithCopy text={displayKey} />
                        </div>
                        <div className={styles.RowChunk}>
                            <TablePropertyValue propertyName={key} propertyValue={displayValue} />
                        </div>
                    </div>
                ))}
            </EmptyHandler>
        </div>
    );
};

