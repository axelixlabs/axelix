import { useState } from "react";
import { Checkbox } from "antd";

import { StyledTag, UniversalModal } from "@/components";

import styles from "./styles.module.css";

const rolesMockData = [
    { name: "VIEWER", description: "Read dashboards, wallboards and values", checked: true },
    { name: "EDITOR", description: "Create and edit dashboards and wallboards", checked: false },
    { name: "MCP Ops", description: "Operate and configure the MCP bridge servers", checked: false },
    { name: "Quality Analyst", description: "Read dashboards and their values, no changes", checked: false },
];

export const ManageIncludedRoles = () => {
    const [open, setOpen] = useState<boolean>(false);

    const onClose = (): void => {
        setOpen(false);
    }

    return (
        <>
            <div onClick={() => setOpen(true)} className={styles.Manage}>
                Manage
            </div>

            <UniversalModal
                open={open}
                title="Included roles"
                subtitle="Wallboard Operator inherits every grant of the roles you include here. Grants it holds directly are unaffected."
                onOk={onClose}
                onClose={onClose}
                okText="Save inclusions"
                footerExtra={
                    <div className={styles.FooterExtra}>
                        Effective: <span className={styles.EffectiveCount}>7 of 18</span> permissions
                    </div>
                }
            >
                {rolesMockData.map(({ checked, name, description }) => (
                    <div key={name} className={styles.RoleRow}>
                        <Checkbox checked={checked} />
                        <StyledTag>{name}</StyledTag>
                        <div>{description}</div>
                    </div>
                ))}

                <div className={`TextUltraSmall ${styles.DisabledNote}`}>
                    Shift Supervisor cannot be included - it already includes this role.
                </div>
            </UniversalModal>
        </>
    );
};