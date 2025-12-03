import { AutoComplete, type AutoCompleteProps, Input } from "antd";
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
    removeBottomGutter?: boolean;

    /**
     * Optional text to display after the search field
     */
    addonAfter?: string;

    /**
     * Options for the autocomplete input.
     */
    autocompleteOptions?: AutoCompleteProps["options"];
}

export const PageSearch = ({ setSearch, addonAfter, autocompleteOptions, removeBottomGutter }: IProps) => {
    const { t } = useTranslation();

    const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);

    const scheduleSetSearch = (value: string): void => {
        if (debounceRef.current) {
            clearTimeout(debounceRef.current);
        }

        debounceRef.current = setTimeout(() => setSearch(value), 500);
    };

    if (autocompleteOptions) {
        return (
            <AutoComplete
                options={autocompleteOptions}
                onChange={(value) => scheduleSetSearch(value)}
                onSelect={(value) => setSearch(value)}
                className={`${styles.Search} ${removeBottomGutter ? styles.RemovedBottomGutter : ""} `}
            >
                <Input placeholder={t("search")} addonAfter={addonAfter} />
            </AutoComplete>
        );
    }

    return (
        <Input
            placeholder={t("search")}
            addonAfter={addonAfter}
            onChange={(e) => scheduleSetSearch(e.target.value)}
            className={`${styles.Search} ${removeBottomGutter ? styles.RemovedBottomGutter : ""} `}
        />
    );
};
