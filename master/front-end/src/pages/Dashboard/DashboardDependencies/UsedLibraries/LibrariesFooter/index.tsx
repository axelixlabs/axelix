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

import { DEPENDENCY_HEALTH_PAGE_SIZE } from "@/utils";

import styles from "./styles.module.css";

interface IProps {
    page: number;
    shownLibraries: number;
    matchingLibraries: number;
    onPageChange: (page: number) => void;
}

export const LibrariesFooter = ({ page, shownLibraries, matchingLibraries, onPageChange }: IProps) => {
    const { t } = useTranslation();

    const pageStart = (page - 1) * DEPENDENCY_HEALTH_PAGE_SIZE;

    return (
        <>
            <div className={`TextUltraSmall ${styles.MainWrapper}`}>
                <span>
                    {shownLibraries
                        ? t("Dashboard.Dependencies.footer", {
                              from: pageStart + 1,
                              to: pageStart + shownLibraries,
                              total: matchingLibraries,
                          })
                        : t("Dashboard.Dependencies.noMatch")}
                </span>
                <Pagination
                    size="small"
                    current={page}
                    pageSize={DEPENDENCY_HEALTH_PAGE_SIZE}
                    total={matchingLibraries}
                    showSizeChanger={false}
                    onChange={onPageChange}
                />
            </div>
        </>
    );
};
