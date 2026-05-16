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
import { Tag } from "antd";
import type { Dispatch, SetStateAction } from "react";
import { useNavigate } from "react-router";

import { PageSearch } from "components";
import { getBeanShortName } from "helpers";
import type { IBean } from "models";

import styles from "./styles.module.css";

import { CloseIcon } from "assets";

interface IProps {
    /**
     * Text to display after the search field
     */
    addonAfter?: string;

    /**
     * SetState to update the search string
     */
    setSearch: Dispatch<SetStateAction<string>>;

    /**
     * Selected bean
     */
    selectedBean: IBean | null;

    /**
     * Setter to set the selected bean
     */
    setSelectedBean: Dispatch<SetStateAction<IBean | null>>;
}

export const BeansFirstSection = ({ addonAfter, setSearch, selectedBean, setSelectedBean }: IProps) => {
    const navigate = useNavigate();

    const clearSelectedBean = (): void => {
        setSelectedBean(null);
        navigate(
            {
                hash: "",
            },
            {
                replace: true,
            },
        );
    };

    return (
        <div className={styles.MainWrapper}>
            <PageSearch addonAfter={addonAfter} setSearch={setSearch} removeBottomGutter onSearch={clearSelectedBean} />
            {selectedBean && (
                <div className={styles.SelectedBeanTagWrapper}>
                    <Tag className={styles.Tag}>
                        {getBeanShortName(selectedBean.beanName)}
                        <CloseIcon onClick={clearSelectedBean} className={styles.CloseIcon} />
                    </Tag>
                </div>
            )}
        </div>
    );
};
