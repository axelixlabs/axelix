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
import { Button, Checkbox, Col, Form, Input, Row, Select } from "antd";
import { useState } from "react";

import { UniversalModal } from "@/components";

import styles from "./styles.module.css";

const resourceRows = [
    {
        name: "Dashboards",
        desc: "Read and read values",
        checks: [true, true, false],
    },
    {
        name: "Wallboards",
        desc: "Read, read values and modify",
        checks: [true, true, true],
    },
    {
        name: "MCP servers",
        desc: "All servers",
        checks: [true, false, false],
    },
    {
        name: "Users",
        desc: "Read only",
        checks: [true, false, false],
    },
    {
        name: "Roles",
        desc: "View only, cannot assign",
        checks: [true, false, false],
    },
    {
        name: "Audit log",
        desc: "Not granted",
        checks: [false, false, false],
    },
];

export const CreateRole = () => {
    const [isCreateRoleModalOpen, setIsCreateRoleModalOpen] = useState<boolean>(false);

    const onClose = (): void => {
        setIsCreateRoleModalOpen(false);
    };

    return (
        <>
            <Button type="primary" onClick={() => setIsCreateRoleModalOpen(true)}>
                Create role
            </Button>

            <UniversalModal
                title="Create role"
                subtitle="Cloned from Wallboard Operator"
                open={isCreateRoleModalOpen}
                onOk={onClose}
                onClose={onClose}
                okText="Create role"
                footerExtra={
                    <div className={`TextUltraSmall ${styles.FooterText}`}>
                        Effective: 8 of 18 permissions · 0 members yet
                    </div>
                }
            >
                <Row gutter={16}>
                    <Col span={12}>
                        <Form.Item name="role-name" label="Role name" layout="vertical">
                            <Input defaultValue="Quality Supervisor" />
                        </Form.Item>
                    </Col>
                    <Col span={12}>
                        <Form.Item name="includes-roles" label="Includes roles" layout="vertical">
                            <Select placeholder="add" />
                        </Form.Item>
                    </Col>
                </Row>

                <div className={styles.SectionTitle}>Permissions</div>

                <div className="CustomTable">
                    <div className={`TableHeader ${styles.TableRow}`}>
                        <div className="TableRowChunk">RESOURCE</div>
                        <div className="TableRowChunk">READ</div>
                        <div className="TableRowChunk">READ VALUES</div>
                        <div className="TableRowChunk">MODIFY</div>
                    </div>

                    {resourceRows.map(({ name, desc, checks }) => (
                        <div className={styles.TableRow} key={name}>
                            <div className="TableRowChunk">
                                <div className={styles.ResourceName}>{name}</div>
                                <div className={`TextSmall ${styles.ResourceDesc}`}>{desc}</div>
                            </div>
                            {checks.map((checked, i) => (
                                <div className={`TableRowChunk ${styles.CheckboxWrapper}`} key={i}>
                                    <Checkbox defaultChecked={checked} />
                                </div>
                            ))}
                        </div>
                    ))}
                </div>
            </UniversalModal>
        </>
    );
};
