import { Table } from 'antd';
import { type PropsWithChildren } from 'react';

import { TooltipWithCopy } from 'components/TooltipWithCopy';
import type { ColumnsType } from 'antd/es/table';
import { TableProperty } from './TableProperty';
import type { IKeyValuePair } from 'models';

import styles from './styles.module.css'

interface IProps {
    /**
     * Table header name
     */
    headerName: string;
    /**
     * Table key value data
     */
    properties: IKeyValuePair[];
}

export const TableSection = ({ headerName, properties, children }: PropsWithChildren<IProps>) => {

    const createTableColumns = (): ColumnsType<IKeyValuePair> => {
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
            render: (_, { key, value }) => (
                <div className={styles.TableRow}>
                    <div className={styles.RowFirstChunk}>
                        <TooltipWithCopy text={key} />
                    </div>
                    <div className={styles.RowSecondChunk}>
                        <TableProperty propertyKey={key} propertyValue={value} />
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