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
import { Badge, Button, Checkbox, Input, Select, Tag } from "antd";
import type { Dispatch, SetStateAction } from "react";

import { PageSearch } from "@/components";

import styles from "./styles.module.css";
import { SearchOutlined } from "@ant-design/icons";

interface IProps {
    /**
     * Text to display after the search field
     */
    addonAfter: string;

    /**
     * Setter to update the search
     */
    setSearch: Dispatch<SetStateAction<string>>;
}

export const TransportSelect = () => {
    return (
        <Select
            mode="multiple"
            showSearch={false}
            placeholder="Transport"
            value={["MCP"]}
            open={false}
            classNames={{
                popup: {
                    root: styles.FilterDropdown
                }
            }}
            tagRender={() => <></>}
            popupRender={() => (
                <div className={styles.DropdownBody}>
                    <div className={styles.OptionRow}>
                        <Checkbox checked={false} />
                        <Tag color="orange">WEB</Tag>
                        <span>Browser session</span>
                    </div>
                    <div className={styles.OptionRow}>
                        <Checkbox checked />
                        <Tag color="blue">MCP</Tag>
                        <span>Agent / tool call</span>
                    </div>
                    <div className={styles.DropdownFooter}>
                        <span className={styles.SelectedCount}>1 of 2 selected</span>
                        <div className={styles.FooterActions}>
                            <Button type="link">Clear</Button>
                            <Button type="primary" style={{ background: "#2e7d32" }}>
                                Apply
                            </Button>
                        </div>
                    </div>
                </div>
            )}
        />
    );
};


export const StatusSelect = () => {
    return (
        <Select
            mode="multiple"
            showSearch={false}
            placeholder="Status"
            value={["UNAUTHENTICATED", "ACCESS_DENIED"]}
            open={false}
            classNames={{
                popup: {
                    root: styles.FilterDropdown
                }
            }}
            maxTagCount={0}
            maxTagPlaceholder={() => "2"}
            popupRender={() => (
                <div className={styles.DropdownBody}>
                    <div className={styles.OptionRow}>
                        <Checkbox checked={false} />
                        <Badge status="success" />
                        <span>SUCCESS</span>
                    </div>
                    <div className={styles.OptionRow}>
                        <Checkbox checked />
                        <Badge color="#999" />
                        <span>UNAUTHENTICATED</span>
                    </div>
                    <div className={styles.OptionRow}>
                        <Checkbox checked />
                        <Badge status="error" />
                        <span>ACCESS_DENIED</span>
                    </div>
                    <div className={styles.DropdownFooter}>
                        <span className={styles.SelectedCount}>Failures only</span>
                        <div className={styles.FooterActions}>
                            <Button type="link">Clear</Button>
                            <Button type="primary" style={{ background: "#2e7d32" }}>
                                Apply
                            </Button>
                        </div>
                    </div>
                </div>
            )}
        />
    );
};

export const TimeSelect = () => {
    const relativeOptions = [
        "Last 15 minutes",
        "Last hour",
        "Last 24 hours",
        "Last 7 days",
        "Last 30 days",
    ];

    return (
        <Select
            showSearch={false}
            placeholder="Time"
            labelInValue
            value={{ label: "Last 24 hours", value: "24h" }}
            open={false}
            classNames={{
                popup: {
                    root: styles.FilterDropdown
                }
            }}
            popupRender={() => (
                <div className={styles.DropdownBody}>
                    <div className={styles.SectionLabel}>RELATIVE</div>
                    {relativeOptions.map((option) => (
                        <div
                            key={option}
                            className={
                                option === "Last 24 hours"
                                    ? `${styles.TimeOption} ${styles.TimeOptionActive}`
                                    : styles.TimeOption
                            }
                        >
                            {option === "Last 24 hours" && <span className={styles.CheckMark}>✓</span>}
                            <span>{option}</span>
                        </div>
                    ))}

                    <div className={styles.SectionLabel}>ABSOLUTE</div>
                    <div className={styles.FieldRow}>
                        <span className={styles.FieldLabel}>From</span>
                        <Input value="2026-09-01 18:00" readOnly />
                    </div>
                    <div className={styles.FieldRow}>
                        <span className={styles.FieldLabel}>To</span>
                        <Input value="2026-09-02 18:00" readOnly />
                    </div>

                    <div className={styles.DropdownFooter}>
                        <span className={styles.SelectedCount}>Timezone UTC+2</span>
                        <div className={styles.FooterActions}>
                            <Button type="link">Reset</Button>
                            <Button type="primary" style={{ background: "#2e7d32" }}>
                                Apply
                            </Button>
                        </div>
                    </div>
                </div>
            )}
        />
    );
};

export const AuditLogSearchBar = ({ addonAfter, setSearch }: IProps) => {

    return (
        <>
            <div className={styles.MainWrapper}>
                <div className={styles.SearchBarWrapper}>
                    <PageSearch addonAfter={addonAfter} setSearch={setSearch} removeBottomGutter />
                    <div className={styles.FiltersWrapper}>
                        <OperationSelect />
                        <StatusSelect />
                        <TransportSelect />
                        <TimeSelect />
                    </div>
                </div>
                <div className={styles.ShownItemsInfo}>1-10 of 412</div>
            </div>
        </>
    );
};

const groups: Record<string, { key: string; checked?: boolean }[]> = {
    LOGGERS: [
        { key: "logger:reset", checked: true },
        { key: "logger:set-level" },
        { key: "logger-group:change-level" },
    ],
    CACHES: [
        { key: "caches:enable" },
        { key: "caches:evict", checked: true },
        { key: "caches:read" },
    ],
    METRICS: [{ key: "metrics:read-one" }, { key: "metrics:read-all" }],
    BEANS: [],
};

export const OperationSelect = () => {
    return (
        <Select
            mode="multiple"
            showSearch={false}
            placeholder="Operation"
            value={["logger:reset", "caches:evict"]}
            open={false}
            classNames={{
                popup: {
                    root: styles.FilterDropdown
                }
            }}
            maxTagCount={0}
            maxTagPlaceholder={() => "2"}
            popupRender={() => (
                <div className={styles.DropdownBody}>
                    <Input placeholder="Search operation..." prefix={<SearchOutlined />} />

                    <div className={styles.ScrollArea}>
                        {Object.entries(groups).map(([group, items]) => (
                            <div key={group}>
                                <div className={styles.SectionLabel}>{group}</div>
                                {items.map((item) => (
                                    <div key={item.key} className={styles.OptionRow}>
                                        <Checkbox checked={!!item.checked} />
                                        <span>{item.key}</span>
                                    </div>
                                ))}
                            </div>
                        ))}
                    </div>

                    <div className={styles.DropdownFooter}>
                        <span className={styles.SelectedCount}>2 of 13 selected</span>
                        <div className={styles.FooterActions}>
                            <Button type="link">Clear</Button>
                            <Button type="primary" style={{ background: "#2e7d32" }}>
                                Apply
                            </Button>
                        </div>
                    </div>
                </div>
            )}
        />
    );
};