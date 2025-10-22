import { Input } from "antd";
import classNames from "classnames";
import type { Dispatch, SetStateAction } from "react";
import { useTranslation } from "react-i18next";

import styles from "./styles.module.css";

interface IProps {
    /**
     * The value of the search
     */
    search: string;

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

export const PageSearch = ({ setSearch, search, addonAfter, hasBottomGutter = true }: IProps) => {
    const { t } = useTranslation();

    return (
        <Input
            placeholder={t("search")}
            addonAfter={addonAfter}
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className={classNames(styles.Search, { [styles.BottomGutter]: hasBottomGutter })}
        />
    );
};
