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
import { Pagination } from "antd";
import { useTranslation } from "react-i18next";

import { SPRING_PORTFOLIO_APPLICATIONS_PAGE_SIZE } from "@/utils";

import styles from "./styles.module.css";

interface IProps {
    page: number;
    shownApplications: number;
    matchingApplications: number;
    onPageChange: (page: number) => void;
}

export const ApplicationsFooter = ({ page, shownApplications, matchingApplications, onPageChange }: IProps) => {
    const { t } = useTranslation();

    const pageStart = (page - 1) * SPRING_PORTFOLIO_APPLICATIONS_PAGE_SIZE;

    return (
        <>
            <div className={`TextUltraSmall ${styles.MainWrapper}`}>
                <span>
                    {shownApplications
                        ? t("Dashboard.SpringPortfolio.applicationsFooter", {
                              from: pageStart + 1,
                              to: pageStart + shownApplications,
                              total: matchingApplications,
                          })
                        : t("Dashboard.SpringPortfolio.applicationsFooterEmpty")}
                </span>
                <Pagination
                    size="small"
                    current={page}
                    pageSize={SPRING_PORTFOLIO_APPLICATIONS_PAGE_SIZE}
                    total={matchingApplications}
                    showSizeChanger={false}
                    onChange={onPageChange}
                />
            </div>
        </>
    );
};
