import { Checkbox } from "antd";
import { useTranslation } from "react-i18next";

import styles from "../styles.module.css";

interface IProps {
    /**
     * A string values tag. This 'tag' serves as the key in the i18n dictionary, which in turn represents the
     * short technical term, that describes what {@link value} flag really represents (i.e. {@link IBean.isLazyInit},
     * or {@link IBean.isPrimary} etc.)
     */
    valueTag: string;

    /**
     * The value of the boolean flag (on / off)
     */
    value: boolean;
}

export const BeanBooleanFlag = ({ value, valueTag }: IProps) => {
    const { t } = useTranslation();

    return (
        <>
            <div className={styles.CollapseBodyChunkTitle}>{t(`Beans.${valueTag}`)}:</div>
            <Checkbox checked={value} />
        </>
    );
};
