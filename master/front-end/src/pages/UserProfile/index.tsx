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
import { App, Button, Modal } from "antd";
import { useCallback, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { Link, useParams } from "react-router";

import { EmptyHandler, Loader, StyledTag } from "@/components";
import {
    extractErrorCode,
    fetchData,
    getUserDisplayStatus,
    getUserFullName,
    getUserInitials,
    showErrorNotification,
} from "@/helpers";
import { EUserDisplayStatus, EUserOrigin, EUserStatus, type IUser, StatefulRequest, StatelessRequest } from "@/models";
import { getUserById, updateUsersStatus } from "@/services";

import { ChangeRoles } from "./ChangeRoles";
import { EditUser } from "./EditUser";
import { UserProfileActions } from "./UserProfileActions";
import { PERMISSION_GROUPS, compareRoles, effectiveAuthorities } from "./permissions";
import styles from "./styles.module.css";

const MISSING_VALUE = "—";

const statusClassNames: Record<EUserDisplayStatus, string> = {
    [EUserDisplayStatus.ACTIVE]: styles.StatusActive,
    [EUserDisplayStatus.DORMANT]: styles.StatusDormant,
    [EUserDisplayStatus.SUSPENDED]: styles.StatusSuspended,
};

const formatSignIn = (value: string, language: string): string => {
    return new Intl.DateTimeFormat(language, {
        day: "2-digit",
        month: "short",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
    }).format(new Date(value));
};

interface IProfileProps {
    user: IUser;
    displayName: string;
    givenName: string;
}

const ProfileHeader = ({
    user,
    displayName,
    onEdit,
    onSuspend,
}: IProfileProps & { onEdit: () => void; onSuspend: () => void }) => {
    const { t } = useTranslation();
    const status = getUserDisplayStatus(user);
    const canSignIn = user.status !== EUserStatus.SUSPENDED;

    return (
        <>
            <div className={styles.Crumb}>
                <Link to="/administration/users">{t("Users.title")}</Link>
                <span>/</span>
                <b>{displayName}</b>
            </div>

            <div className={styles.Identity}>
                <span className={`${styles.Avatar} ${getUserFullName(user) ? "" : styles.AvatarAnonymous}`}>
                    {getUserInitials(user)}
                </span>
                <div className={styles.IdentityText}>
                    <div className={styles.NameRow}>
                        <h1 className={styles.Name}>{displayName}</h1>
                        <span className={`${styles.Status} ${statusClassNames[status]}`}>
                            <i />
                            {t(`Users.Statuses.${status}`)}
                        </span>
                    </div>
                    <p className={styles.Meta}>
                        <span className={styles.Mono}>{user.username}</span>
                        {user.email && <span> · {user.email}</span>}
                    </p>
                </div>
                <div className={styles.Actions}>
                    <Button onClick={onEdit}>{t("Users.Profile.editUser")}</Button>
                    {user.userOrigin === EUserOrigin.LOCAL && (
                        <Button danger={canSignIn} onClick={onSuspend}>
                            {t(canSignIn ? "Users.Profile.suspend" : "Users.Profile.reactivate")}
                        </Button>
                    )}
                </div>
            </div>
        </>
    );
};

const ProfileDetails = ({ user }: { user: IUser }) => {
    const { t, i18n } = useTranslation();
    const status = getUserDisplayStatus(user);
    const canSignIn = user.status !== EUserStatus.SUSPENDED;
    const isLocal = user.userOrigin === EUserOrigin.LOCAL;
    const lastSignIn = user.lastLoginAt ? formatSignIn(user.lastLoginAt, i18n.language) : t("Users.never");

    return (
        <div>
            <section className={styles.Card}>
                <h2 className={`${styles.CardTitle} ${styles.CardHead}`}>{t("Users.Profile.profile")}</h2>
                <Field label={t("Users.Create.firstName")} value={user.firstName} />
                <Field label={t("Users.Create.lastName")} value={user.lastName} />
                <Field label={t("username")} value={user.username} mono />
                <Field label={t("Users.Profile.email")} value={user.email} />
            </section>

            <section className={styles.Card}>
                <h2 className={`${styles.CardTitle} ${styles.CardHead}`}>{t("Users.Profile.account")}</h2>
                <div className={styles.Field}>
                    <span>{t("Users.origin")}</span>
                    <b>
                        {t(isLocal ? "Users.originLocal" : "Users.originOidc")}
                        <span className={styles.Muted}>
                            {" "}
                            · {t(isLocal ? "Users.Profile.originLocalDetail" : "Users.Profile.originOidcDetail")}
                        </span>
                    </b>
                </div>
                <div className={styles.Field}>
                    <span>{t("Users.status")}</span>
                    <b>
                        {t(`Users.Statuses.${status}`)}
                        <span className={styles.Muted}>
                            {" "}
                            · {t(canSignIn ? "Users.Profile.canSignIn" : "Users.Profile.signInBlocked")}
                        </span>
                    </b>
                </div>
                <Field label={t("Users.Profile.lastSignIn")} value={lastSignIn} />
            </section>

            <UserProfileActions userId={user.id} userOrigin={user.userOrigin} />
        </div>
    );
};

const RolePermissions = ({ user, onChangeRoles }: { user: IUser; onChangeRoles: () => void }) => {
    const { t } = useTranslation();
    const granted = effectiveAuthorities(user.roles);
    const heldRoles = [...user.roles].sort((left, right) => compareRoles(right, left));

    return (
        <div>
            <section className={styles.Card}>
                <div className={`${styles.CardHeader} ${styles.CardHead}`}>
                    <h2 className={styles.CardTitle}>{t("Users.roles")}</h2>
                    <Button type="link" className={styles.ChangeRoles} onClick={onChangeRoles}>
                        {t("Users.Profile.changeRoles")}
                    </Button>
                </div>
                {heldRoles.map((role) => (
                    <div key={role} className={styles.Role}>
                        <StyledTag>{role}</StyledTag>
                        <p>{t(`Users.Profile.RoleDescriptions.${role}`)}</p>
                    </div>
                ))}
            </section>

            <section className={styles.Card}>
                <div className={styles.CardHead}>
                    <h2 className={styles.CardTitle}>{t("Users.Profile.permissions")}</h2>
                    <p className={styles.CardHint}>{t("Users.Profile.permissionsHint")}</p>
                </div>
                {PERMISSION_GROUPS.map((group) => (
                    <div key={group.id} className={styles.GroupBlock}>
                        <div className={`TextCaps ${styles.Group}`}>{t(`Users.Profile.Groups.${group.id}`)}</div>
                        {group.authorities.map((authority) => {
                            const source = granted.get(authority);

                            return (
                                <div key={authority} className={`${styles.Permission} ${source ? "" : styles.Off}`}>
                                    {source ? <GrantedMark /> : <span className={styles.Denied} />}
                                    <span>
                                        {t(`Users.Profile.Authorities.${authority}`)}
                                        <span className={styles.Code}>{authority}</span>
                                    </span>
                                    <span className={styles.Source}>{source ?? MISSING_VALUE}</span>
                                </div>
                            );
                        })}
                    </div>
                ))}
            </section>
        </div>
    );
};

const SuspendUser = ({
    user,
    displayName,
    open,
    onClose,
    onSaved,
}: IProfileProps & {
    open: boolean;
    onClose: () => void;
    onSaved: () => void;
}) => {
    const { t } = useTranslation();
    const { message } = App.useApp();
    const [requestData, setRequestData] = useState(StatelessRequest.inactive());
    const canSignIn = user.status !== EUserStatus.SUSPENDED;

    const confirm = (): void => {
        const nextStatus = canSignIn ? EUserStatus.SUSPENDED : EUserStatus.ACTIVE;
        setRequestData(StatelessRequest.loading());

        updateUsersStatus([user.id], nextStatus)
            .then(() => {
                setRequestData(StatelessRequest.success());
                message.success(t(canSignIn ? "Users.Profile.userSuspended" : "Users.Profile.userReactivated"));
                onClose();
                onSaved();
            })
            .catch((error) => {
                const errorCode = extractErrorCode(error?.response?.data);
                setRequestData(StatelessRequest.error(errorCode));
                showErrorNotification(errorCode);
            });
    };

    return (
        <Modal
            open={open}
            onCancel={onClose}
            onOk={confirm}
            centered
            confirmLoading={requestData.loading}
            okText={t(canSignIn ? "Users.Profile.suspendConfirm" : "Users.Profile.reactivateConfirm")}
            cancelText={t("cancel")}
            okButtonProps={canSignIn ? { danger: true } : undefined}
            title={t(canSignIn ? "Users.Profile.suspendTitle" : "Users.Profile.reactivateTitle", { name: displayName })}
        >
            <p>{t(canSignIn ? "Users.Profile.suspendBody" : "Users.Profile.reactivateBody", { name: displayName })}</p>
            {canSignIn && (
                <div className={styles.SuspendNotes}>
                    <p>{t("Users.Profile.suspendKept")}</p>
                    <p>{t("Users.Profile.suspendLater")}</p>
                </div>
            )}
        </Modal>
    );
};

const UserProfile = () => {
    const { userId } = useParams();
    const [userResponse, setUserResponse] = useState(StatefulRequest.loading<IUser>());
    const [editing, setEditing] = useState(false);
    const [changingRoles, setChangingRoles] = useState(false);
    const [suspending, setSuspending] = useState(false);

    const loadUser = useCallback((): void => {
        setUserResponse(StatefulRequest.loading<IUser>());
        fetchData(setUserResponse, () => getUserById(userId!));
    }, [userId]);

    useEffect(() => {
        loadUser();
    }, [loadUser]);

    if (userResponse.loading) {
        return <Loader />;
    }

    if (userResponse.error || !userResponse.response) {
        return <EmptyHandler isEmpty />;
    }

    const user = userResponse.response;
    const displayName = getUserFullName(user) ?? user.username;
    const givenName = user.firstName?.trim() || displayName;

    return (
        <>
            <ProfileHeader
                user={user}
                displayName={displayName}
                givenName={givenName}
                onEdit={() => setEditing(true)}
                onSuspend={() => setSuspending(true)}
            />
            <div className={styles.Columns}>
                <ProfileDetails user={user} />
                <RolePermissions user={user} onChangeRoles={() => setChangingRoles(true)} />
            </div>
            <EditUser user={user} open={editing} onClose={() => setEditing(false)} onSaved={loadUser} />
            <ChangeRoles
                user={user}
                displayName={displayName}
                givenName={givenName}
                open={changingRoles}
                onClose={() => setChangingRoles(false)}
                onSaved={loadUser}
            />
            <SuspendUser
                user={user}
                displayName={displayName}
                givenName={givenName}
                open={suspending}
                onClose={() => setSuspending(false)}
                onSaved={loadUser}
            />
        </>
    );
};

const Field = ({ label, value, mono = false }: { label: string; value: string | null; mono?: boolean }) => {
    return (
        <div className={styles.Field}>
            <span>{label}</span>
            <b className={mono ? styles.Mono : undefined}>{value?.trim() ? value : MISSING_VALUE}</b>
        </div>
    );
};

const GrantedMark = () => {
    return (
        <span className={styles.Granted} aria-hidden>
            <svg width="11" height="11" viewBox="0 0 24 24" fill="none">
                <path
                    d="M20 6 9 17l-5-5"
                    stroke="currentColor"
                    strokeWidth="3"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                />
            </svg>
        </span>
    );
};

export default UserProfile;
