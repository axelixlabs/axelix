import { Input } from 'antd';
import classNames from 'classnames';
import { useTranslation } from 'react-i18next';

import styles from './styles.module.css'

interface IProps {
    /**
     * Callback function when the search value changes
     */
    onChange: (value: string) => void;
    /**
     * Whether to add a bottom gutter to the search field
     */
    hasBottomGutter?: boolean;
    /**
     * Optional text to display after the search field
     */
    addonAfter: string
}

export const PageSearch = ({ onChange, addonAfter, hasBottomGutter = true }: IProps) => {
    const { t } = useTranslation()

    return (
        <Input
            placeholder={t("search")}
            addonAfter={addonAfter}
            onChange={(e) => onChange(e.target.value)}
            className={classNames(
                styles.Search,
                { [styles.BottomGutter]: hasBottomGutter }
            )} />
    )
};