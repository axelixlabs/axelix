import { Button, Form, Select } from "antd";
import { AppAlert, UniversalModal } from "@/components";

import styles from "./styles.module.css";
import { useState } from "react";
import { Link } from "react-router";

export const DeleteRole = () => {
    const [isModalOpen, setIsModalOpen] = useState<boolean>(false);

    const onClose = (): void => {
        setIsModalOpen(false);
    }

    return (
        <>
            <Button danger onClick={() => setIsModalOpen(true)}>Delete role</Button>

            <UniversalModal
                title="Delete Wallboard Operator?"
                subtitle="The role and its 7 grants are removed. Two things depend on it right now."
                open={isModalOpen}
                onClose={onClose}
                onOk={onClose}
                okText="Delete role"
                footerExtra={<div className={`TextUltraSmall ${styles.SecondaryText}`}>Cannot be undone</div>}
            >
                <div className={styles.InfoCard}>
                    <div className={styles.InfoCardTitle}>19 members hold this role directly</div>
                    <div className={styles.InfoCardDescription}>They keep any other roles they have. 14 of them would be left with no role at all. A further 3 members inherit it via Shift Supervisor and are unaffected.</div>
                    <Form.Item label={<div className={`TextUltraSmall ${styles.SecondaryText}`}>Give those 14 members instead</div>} layout="vertical">
                        <Select />
                    </Form.Item>
                </div>

                <AppAlert title="Shift Supervisor includes this role" type="error">
                    <div className={styles.SecondaryText}>
                        Deleting it would silently drop 7 grants from Shift Supervisor. Remove the inclusion there first.
                    </div>
                    <Link to="#" className="AccentedLink">Open Shift Supervisor</Link>
                </AppAlert>
            </UniversalModal>
        </>
    );
};
