import { useTranslation } from "react-i18next";

import type { IDetailsCardRecord } from "models";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Icon for the details card
     */
    icon: string;
    /**
     * Details card title
     */
    title: string;
    /**
     * Prefix to be used when translating {@link records} via i18n.
     */
    i18nPropertiesPrefix: string;
    /**
     * Details card records
     */
    records: IDetailsCardRecord[];
}

export const DetailsCard = ({ icon, i18nPropertiesPrefix, title, records }: IProps) => {
    const { t } = useTranslation();

    return (
        <div className={`CustomizedAntdTable ${styles.Card}`}>
            <div className="TableHeader">
                <div className={`RowChunk ${styles.TableHeaderRowChunk}`}>
                    {icon && <img src={icon} alt={`${title} icon`} className={styles.CardIcon} />}
                    {t(title)}
                </div>
            </div>

            {records.map(({ key, value }) => (
                <div className="TableRow" key={key}>
                    <div className="RowChunk">{t(`${i18nPropertiesPrefix}.${key}`)}</div>
                    <div className="RowChunk">
                        <div className={styles.ValueWrapper}>{value}</div>
                    </div>
                </div>
            ))}
        </div>
    );
};
