import { Table } from 'antd';

import styles from './styles.module.css'
import { TooltipWithCopy } from 'components/TooltipWithCopy';
import type { ColumnsType } from 'antd/es/table';
import type { IKeyValuePair } from 'models';

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
     * Config props prefix
     */
    prefix?: string
}

export const TableData = ({ name, properties, prefix }: IProps) => {

    const createTableColumns = (): ColumnsType<IKeyValuePair> => {
        return [
            {
                key: name,
                title: (
                    <>
                        <div>{name}</div>
                        {prefix && (
                            <div className={styles.Prefix}>
                                <span className={styles.PrefixTitle}>Prefix:</span> {prefix}
                            </div>
                        )}
                    </>
                ),
                onHeaderCell: () => ({
                    style: { backgroundColor: "#00AB551A", wordBreak: "break-all" },
                }),
                render: (_, { key, value }) => (
                    <div className={styles.TableRow}>
                        <div className={styles.RowChunk}>
                            <TooltipWithCopy text={key} />
                        </div>
                        <div className={styles.RowChunk}>{value ?? 'null'}</div>
                    </div>
                ),
            },
        ];
    };

    return (
        <Table
            columns={createTableColumns()}
            dataSource={properties}
            pagination={false}
            className={styles.Table}
        />
    )
};