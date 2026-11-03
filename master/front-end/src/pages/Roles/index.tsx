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
import { Button } from "antd";
import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";

import { EmptyHandler, Loader, StyledTag } from "@/components";
import { countUsersWithRole, fetchData } from "@/helpers";
import { ERoles, type IUser, StatefulRequest } from "@/models";
import { getUsers } from "@/services";
import { ENTERPRISE_URL } from "@/utils";

import styles from "./styles.module.css";

// Most privileged first, matching the Roles & permissions screen
const BUILTIN_ROLES = [ERoles.ADMIN, ERoles.EDITOR, ERoles.VIEWER];

const Roles = () => {
    const { t } = useTranslation();
    const [usersData, setUsersData] = useState(StatefulRequest.loading<IUser[]>());

    useEffect(() => {
        fetchData(setUsersData, () => getUsers());
    }, []);

    if (usersData.loading) {
        return <Loader />;
    }

    if (usersData.error) {
        return <EmptyHandler isEmpty />;
    }

    const users = usersData.response!;

    return (
        <>
            <div className={styles.Header}>
                <div className="TextLarge">{t("Roles.title")}</div>
                <p className={`TextSmall ${styles.Subtitle}`}>{t("Roles.subtitle")}</p>
            </div>

            <section className={styles.Roles}>
                {BUILTIN_ROLES.map((role) => (
                    <div key={role} className={styles.Row}>
                        <div>
                            <StyledTag>{role}</StyledTag>
                            <div className={styles.Count}>
                                {t("Users.Create.usersCount", { count: countUsersWithRole(users, role) })}
                            </div>
                        </div>
                        <div>
                            <div className={styles.Description}>{t(`Roles.Builtin.${role}.description`)}</div>
                            <p className={`TextUltraSmall ${styles.Grants}`}>{t(`Roles.Builtin.${role}.grants`)}</p>
                        </div>
                    </div>
                ))}
            </section>

            <section className={styles.Upsell}>
                <div className={styles.UpsellCopy}>
                    <div className={styles.UpsellTitle}>{t("Roles.Enterprise.title")}</div>
                    <p className={`TextSmall ${styles.UpsellBody}`}>{t("Roles.Enterprise.body")}</p>
                </div>
                <Button type="primary" href={ENTERPRISE_URL} target="_blank" rel="noopener noreferrer">
                    {t("Roles.Enterprise.compare")}
                </Button>
            </section>
        </>
    );
};

export default Roles;
