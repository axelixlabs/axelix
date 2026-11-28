/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
import { type SetStateAction, useState } from "react";
import { useTranslation } from "react-i18next";

import { PageSearch } from "@/components";
import { getSpringSliceVersionsCount, matchesSpringSlice } from "@/helpers";
import type { ISpringPortfolioApplication, ISpringSlice } from "@/models";
import { SPRING_PORTFOLIO_APPLICATIONS_PAGE_SIZE, SPRING_SLICE_MIN_APPLICATIONS } from "@/utils";

import { ApplicationRow } from "./ApplicationRow";
import { ApplicationsFooter } from "./ApplicationsFooter";
import { ApplicationsHeader } from "./ApplicationsHeader";
import { SpringSliceFilter } from "./SpringSliceFilter";
import styles from "./styles.module.css";

interface IProps {
    applications: ISpringPortfolioApplication[];
}

export const ApplicationsInUse = ({ applications }: IProps) => {
    const { t } = useTranslation();

    const [search, setSearch] = useState<string>("");
    const [page, setPage] = useState<number>(1);
    const [slice, setSlice] = useState<ISpringSlice | null>(null);

    const changeSearch = (value: SetStateAction<string>): void => {
        setPage(1);
        setSearch(value);
    };

    const changeSlice = (nextSlice: ISpringSlice | null): void => {
        setPage(1);
        setSlice(nextSlice);
    };

    const query = search.trim().toLowerCase();
    const matchingApplications = applications.filter(
        (application) =>
            application.name.toLowerCase().includes(query) && (!slice || matchesSpringSlice(application, slice)),
    );
    const pageStart = (page - 1) * SPRING_PORTFOLIO_APPLICATIONS_PAGE_SIZE;
    const rows = matchingApplications.slice(pageStart, pageStart + SPRING_PORTFOLIO_APPLICATIONS_PAGE_SIZE);

    return (
        <>
            <div className={styles.MainWrapper}>
                <div className={`TextUltraSmall ${styles.CaptionRow}`}>
                    <span className={styles.Caption}>{t("Dashboard.SpringPortfolio.applicationsInUseTitle")}</span>
                    <span>{t("Dashboard.SpringPortfolio.applicationsInUseHint")}</span>
                </div>

                <div className={styles.Table}>
                    <div className={styles.Toolbar}>
                        <PageSearch
                            setSearch={changeSearch}
                            addonAfter={`${matchingApplications.length} / ${applications.length}`}
                            removeBottomGutter
                        />

                        {applications.length >= SPRING_SLICE_MIN_APPLICATIONS &&
                            getSpringSliceVersionsCount(applications) > 1 && (
                                <SpringSliceFilter applications={applications} slice={slice} onChange={changeSlice} />
                            )}
                    </div>

                    <div className={styles.Scroll}>
                        <ApplicationsHeader />

                        {rows.map((application) => (
                            <ApplicationRow key={application.name} application={application} />
                        ))}

                        {rows.length === 0 && (
                            <div className={`TextSmall ${styles.NoMatch}`}>
                                {t("Dashboard.SpringPortfolio.noApplicationsMatch")}
                            </div>
                        )}
                    </div>

                    <ApplicationsFooter
                        page={page}
                        shownApplications={rows.length}
                        matchingApplications={matchingApplications.length}
                        onPageChange={setPage}
                    />
                </div>
            </div>
        </>
    );
};
