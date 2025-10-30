import { useTranslation } from "react-i18next";

import styles from "../styles.module.css";

interface IProps {
    /**
     * A string values tag. This 'tag' serves as the key in the i18n dictionary, which in turn represents the
     * short technical term, that describes what {@link values} really are (i.e. they are qualifiers, aliases etc.)
     */
    valuesTag: string;

    /**
     * An array of values to be displayed.
     */
    values: string[];
}

/**
 * Functional component that represents a list of simple, non-clickable values to be displayed in the
 * bean collapse drop-down.
 */
export const BeanSimpleList = ({ valuesTag, values }: IProps) => {
    const { t } = useTranslation();

    return (
        <>
            <div className={styles.AccordionBodyChunkTitle}>{t(`Beans.${valuesTag}`)}:</div>

            <div>
                {!values.length ? (
                    <span>-</span>
                ) : (
                    values.map((values) => (
                        <div key={values} className={styles.AccordionBodyChunkList}>
                            <div className={styles.SimpleListValue}>{values}</div>
                        </div>
                    ))
                )}
            </div>
        </>
    );
};
