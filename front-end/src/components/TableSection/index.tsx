import { Table } from 'antd';
import { type PropsWithChildren } from 'react';

import { TooltipWithCopy } from 'components/TooltipWithCopy';
import { TablePropertyValue  } from './TablePropertyValue';
import type { ColumnsType } from 'antd/es/table';

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

interface ITableRow {

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

export const ModifiableTableSection = ({ headerName, properties, children }: PropsWithChildren<IProps>) => {

    const createTableColumns = (): ColumnsType<ITableRow> => {
        return [{
            key: headerName,
            title: (
                <>
                    <div>{headerName}</div>
                    {children}
                </>
            ),
            onHeaderCell: () => ({
                style: { backgroundColor: "#00AB551A", wordBreak: "break-all" },
            }),
            render: (_, { displayKey, displayValue, key }) => (
                <div className={styles.TableRow}>
                    <div className={styles.RowFirstChunk}>
                        <TooltipWithCopy text={displayKey} />
                    </div>
                    <div className={styles.RowSecondChunk}>
                        <TablePropertyValue propertyName={key} propertyValue={displayValue} />
                    </div>
                </div>
            ),
        }]
    };

    return (
        <Table
            columns={createTableColumns()}
            dataSource={properties}
            pagination={false}
            bordered
            className={styles.Table}
        />
    )
};