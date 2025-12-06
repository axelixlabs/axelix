import CheckableTag from "antd/es/tag/CheckableTag";
import type { Dispatch, SetStateAction } from "react";
import { useTranslation } from "react-i18next";

import { PageSearch } from "components";
import { ELoggersTabs, type ILoggersSearchFilters } from "models";

import styles from "./styles.module.css";

interface IProps {
    /**
     *  Content to display after the input field
     */
    addonAfter: string;

    /**
     *  Currently active logger tab
     */
    activeKey: ELoggersTabs;

    /**
     * Setter for the search text
     */
    setSearch: Dispatch<SetStateAction<string>>;

    /**
     *  Search filters
     */
    searchFilters: ILoggersSearchFilters;

    /**
     * Setter for the search filters
     */
    setSearchFilters: Dispatch<SetStateAction<ILoggersSearchFilters>>;
}

export const LoggersSearchBar = ({ addonAfter, activeKey, setSearch, searchFilters, setSearchFilters }: IProps) => {
    const { t } = useTranslation();

    const { isConfiguredSearch, isClassOnlySearch } = searchFilters;

    return (
        <div className={styles.SearchBarWrapper}>
            <PageSearch addonAfter={addonAfter} setSearch={setSearch} key={activeKey} removeBottomGutter />
            {activeKey === ELoggersTabs.LOGGERS && (
                <div className={styles.SearchFilterTagsWrapper}>
                    <CheckableTag
                        checked={isConfiguredSearch}
                        onChange={() =>
                            setSearchFilters((prev) => ({
                                ...prev,
                                isConfiguredSearch: !prev.isConfiguredSearch,
                            }))
                        }
                        className={`${styles.SearchFilterTag} ${isConfiguredSearch ? styles.SearchFilterTagSelected : ""}`}
                    >
                        {t("Loggers.configured")}
                    </CheckableTag>

                    <CheckableTag
                        checked={isClassOnlySearch}
                        onChange={() =>
                            setSearchFilters((prev) => ({
                                ...prev,
                                isClassOnlySearch: !prev.isClassOnlySearch,
                            }))
                        }
                        className={`${styles.SearchFilterTag} ${isClassOnlySearch ? styles.SearchFilterTagSelected : ""}`}
                    >
                        {t("Loggers.classOnly")}
                    </CheckableTag>
                </div>
            )}
        </div>
    );
};
