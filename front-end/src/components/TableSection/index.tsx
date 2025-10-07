import { Table } from 'antd';
import { type PropsWithChildren } from 'react';

import { TooltipWithCopy } from 'components/TooltipWithCopy';
import type { ColumnsType } from 'antd/es/table';
import { TableProperty } from './TableProperty';
import type { IKeyValuePair } from 'models';

import styles from './styles.module.css'

interface IProps {
    /**
     * Table title
     */
    name: string;
    /**
     * Table key value data
     */
    properties: IKeyValuePair[];
    /**
     * If editableProperty is true, the property can be edited.
     */
    editableProperty?: boolean;
}

export const TableSection = ({ name, properties, editableProperty, children }: PropsWithChildren<IProps>) => {

    const createTableColumns = (): ColumnsType<IKeyValuePair> => {
        return [{
            key: name,
            title: (
                <>
                    <div>{name}</div>
                    {children}
                </>
            ),
            onHeaderCell: () => ({
                style: { backgroundColor: "#00AB551A", wordBreak: "break-all" },
            }),
            render: (_, { key, value }) => (
                <div className={styles.TableRow}>
                    <div className={styles.RowFirstChunk}>
                        <TooltipWithCopy text={key} />
                    </div>
                    <div className={styles.RowSecondChunk}>
                        <TableProperty editableProperty={editableProperty} propertyKey={key} value={value} />
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