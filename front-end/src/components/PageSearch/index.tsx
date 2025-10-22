import { Input } from "antd";
import classNames from "classnames";
import { type Dispatch, type SetStateAction, useRef } from "react";
import { useTranslation } from "react-i18next";

import styles from "./styles.module.css";

interface IProps {
    /**
     * SetState to update the search string
     */
    setSearch: Dispatch<SetStateAction<string>>;

    /**
     * Whether to add a bottom gutter to the search field
     */
    hasBottomGutter?: boolean;

    /**
     * Optional text to display after the search field
     */
    addonAfter?: string;
}

export const PageSearch = ({ setSearch, addonAfter, hasBottomGutter = true }: IProps) => {
    const { t } = useTranslation();

    const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>): void => {
        if (debounceRef.current) {
            clearTimeout(debounceRef.current);
        }

        debounceRef.current = setTimeout(() => setSearch(e.target.value), 500);
    };

    return (
        <Input
            placeholder={t("search")}
            addonAfter={addonAfter}
            onChange={handleChange}
            className={classNames(styles.Search, { [styles.BottomGutter]: hasBottomGutter })}
        />
    );
};
