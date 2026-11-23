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
import { ApartmentOutlined, ArrowLeftOutlined, FilterOutlined } from "@ant-design/icons";

import { Button, Input, Pagination, Popover, Select, Table, Tag } from "antd";
import { useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams, useSearchParams } from "react-router";

import { type TListenerInvocation, type TPublication, mockPublications } from "./mockEvents";
import styles from "./styles.module.css";

const PAGE_SIZE = 10;
const instances = [...new Set(mockPublications.map((event) => event.instance))];
const statusColors = { failed: "error", success: "success", pending: "warning", none: "default" } as const;
type TStatus = keyof typeof statusColors;

const emptyFilters = { instance: "", status: "", from: "", to: "" };

function getStatus(listeners: TListenerInvocation[]): TStatus {
    if (!listeners.length) return "none";
    if (listeners.some((listener) => listener.durationMs === undefined)) return "pending";
    return listeners.some((listener) => listener.error) ? "failed" : "success";
}

const utcTime = (time: number) => new Date(time).toISOString().slice(11, 23);
const utcDate = (time: number) => new Date(time).toISOString().slice(0, 10);

function Outcome({ status }: { status: TStatus }) {
    const { t } = useTranslation();
    return (
        <Tag
            color={statusColors[status]}
            className={styles.Outcome}
            data-outcome={status}
            styles={
                status === "success"
                    ? {
                          root: {
                              color: "var(--axelix-green-700)",
                              backgroundColor: "var(--axelix-green-50)",
                              borderColor: "var(--axelix-green-200)",
                          },
                      }
                    : undefined
            }
        >
            {t(`ApplicationEvents.status.${status}`)}
        </Tag>
    );
}

function EventMetadata({ event }: { event: TPublication }) {
    const { t } = useTranslation();
    return (
        <dl className={styles.Metadata}>
            <dt>{t("ApplicationEvents.published")}</dt>
            <dd>
                <code>
                    {utcDate(event.publishedAt)} {utcTime(event.publishedAt)} UTC
                </code>
            </dd>
            <dt>{t("ApplicationEvents.instance")}</dt>
            <dd>
                <code>{event.instance}</code>
            </dd>
            {event.callSite && (
                <>
                    <dt>{t("ApplicationEvents.callSite")}</dt>
                    <dd>
                        <code>{event.callSite}</code>
                    </dd>
                </>
            )}
        </dl>
    );
}

function PublicationDetails({ event }: { event: TPublication }) {
    const { t } = useTranslation();
    const [selected, setSelected] = useState(() =>
        Math.max(
            0,
            event.listeners.findIndex((item) => item.error),
        ),
    );
    const listener = event.listeners[selected];
    const startedAt = listener && event.publishedAt + listener.startedAfterMs;

    return (
        <div className={styles.DetailLayout}>
            <section className={styles.Panel}>
                <div className={styles.PanelHeading}>
                    <h2>{t("ApplicationEvents.publicationTree")}</h2>
                    <span>{t("ApplicationEvents.listenerCount", { count: event.listeners.length })}</span>
                </div>
                <div className={styles.Tree}>
                    <div className={styles.Root} data-connected={!!event.listeners.length}>
                        <span>{t("ApplicationEvents.eventPublished")}</span>
                        <strong>
                            <code>{event.type}</code>
                        </strong>
                        <Outcome status={getStatus(event.listeners)} />
                    </div>
                    {event.listeners.length > 0 && (
                        <ul className={styles.Branches} aria-label={t("ApplicationEvents.listeners")}>
                            {event.listeners.map((item, index) => (
                                <li key={item.method}>
                                    <button
                                        type="button"
                                        className={styles.Node}
                                        aria-pressed={selected === index}
                                        onClick={() => setSelected(index)}
                                    >
                                        <strong>
                                            <code>{item.method}</code>
                                        </strong>
                                        <span className={styles.NodeMeta}>
                                            <Outcome status={getStatus([item])} />
                                            {item.durationMs !== undefined && <code>{item.durationMs} ms</code>}
                                        </span>
                                    </button>
                                </li>
                            ))}
                        </ul>
                    )}
                </div>
            </section>
            <section
                className={`${styles.Panel} ${styles.Inspector}`}
                aria-label={t("ApplicationEvents.listenerDetails")}
            >
                {listener ? (
                    <>
                        <Outcome status={getStatus([listener])} />
                        <h2>
                            <code>{listener.method}</code>
                        </h2>
                        <dl className={styles.Metadata}>
                            <dt>{t("ApplicationEvents.started")}</dt>
                            <dd>
                                <code>{utcTime(startedAt)} UTC</code>
                            </dd>
                            <dt>{t("ApplicationEvents.finished")}</dt>
                            <dd>
                                {listener.durationMs === undefined ? (
                                    "—"
                                ) : (
                                    <code>{utcTime(startedAt + listener.durationMs)} UTC</code>
                                )}
                            </dd>
                            <dt>{t("ApplicationEvents.duration")}</dt>
                            <dd>
                                {listener.durationMs === undefined ? (
                                    t("ApplicationEvents.status.pending")
                                ) : (
                                    <code>{listener.durationMs} ms</code>
                                )}
                            </dd>
                            <dt>{t("ApplicationEvents.mode")}</dt>
                            <dd>{listener.mode}</dd>
                        </dl>
                        {listener.error && (
                            <>
                                <div className={styles.Error}>
                                    <strong>
                                        <code>{listener.error.type}</code>
                                    </strong>
                                    <p>{listener.error.message}</p>
                                </div>
                                <details className={styles.Stack} key={listener.method}>
                                    <summary>{t("ApplicationEvents.stackTrace")}</summary>
                                    <pre>{listener.error.stack}</pre>
                                </details>
                            </>
                        )}
                    </>
                ) : (
                    <p>{t("ApplicationEvents.noInvocations")}</p>
                )}
            </section>
        </div>
    );
}

function filterPublications(search: string, filters: typeof emptyFilters, invalidDates: boolean) {
    const from = filters.from ? Date.parse(filters.from + "Z") : -Infinity;
    const to = filters.to ? Date.parse(filters.to + "Z") + 60_000 : Infinity;
    const query = search.trim().toLowerCase();
    return mockPublications.filter(
        (event) =>
            !invalidDates &&
            event.type.toLowerCase().includes(query) &&
            (!filters.instance || event.instance === filters.instance) &&
            (!filters.status || getStatus(event.listeners) === filters.status) &&
            event.publishedAt >= from &&
            event.publishedAt < to,
    );
}

function ApplicationEventsPage() {
    const { t } = useTranslation();
    const [params, setParams] = useSearchParams();
    const [search, setSearch] = useState("");
    const [filters, setFilters] = useState(emptyFilters);
    const [filtersOpen, setFiltersOpen] = useState(false);
    const [page, setPage] = useState(1);
    const [selectedKey, setSelectedKey] = useState("");
    const publication = mockPublications.find((event) => event.key === params.get("publication"));
    const invalidDates = !!(filters.from && filters.to && filters.from > filters.to);
    const matching = filterPublications(search, filters, invalidDates);
    const offset = (page - 1) * PAGE_SIZE;
    const visible = matching.slice(offset, offset + PAGE_SIZE);
    const selected = visible.find((event) => event.key === selectedKey) ?? visible[0];
    const activeFilters =
        Number(!!filters.instance) + Number(!!filters.status) + Number(Boolean(filters.from + filters.to));

    function updateFilters(next: typeof emptyFilters) {
        setFilters(next);
        setPage(1);
    }

    function openPublication(event?: TPublication) {
        const next = new URLSearchParams(params);
        if (event) next.set("publication", event.key);
        else next.delete("publication");
        setParams(next);
    }

    if (publication) {
        return (
            <div className={styles.Page}>
                <header className={styles.Heading}>
                    <div>
                        <h1>{publication.type}</h1>
                        <p>
                            <code>
                                {utcDate(publication.publishedAt)} {utcTime(publication.publishedAt)} UTC ·{" "}
                                {publication.instance}
                            </code>
                        </p>
                    </div>
                    <Button icon={<ArrowLeftOutlined />} onClick={() => openPublication()}>
                        {t("ApplicationEvents.backToFeed")}
                    </Button>
                </header>
                <PublicationDetails key={publication.key} event={publication} />
            </div>
        );
    }

    return (
        <div className={styles.Page}>
            <header className={styles.Heading}>
                <div>
                    <h1>{t("ApplicationEvents.title")}</h1>
                    <p>{t("ApplicationEvents.sampleData")}</p>
                </div>
            </header>
            <div className={styles.Toolbar}>
                <Input
                    allowClear
                    aria-label={t("ApplicationEvents.search")}
                    placeholder={t("ApplicationEvents.search")}
                    value={search}
                    onChange={(e) => {
                        setSearch(e.target.value);
                        setPage(1);
                    }}
                />
                <Popover
                    trigger="click"
                    placement="bottomLeft"
                    open={filtersOpen}
                    onOpenChange={setFiltersOpen}
                    content={
                        <div
                            className={styles.Filters}
                            onKeyDown={(e) => {
                                if (e.key === "Escape") setFiltersOpen(false);
                            }}
                        >
                            <div className={styles.FilterGrid}>
                                <label>
                                    {t("ApplicationEvents.instance")}
                                    <Select
                                        aria-label={t("ApplicationEvents.instance")}
                                        value={filters.instance}
                                        onChange={(instance) => updateFilters({ ...filters, instance })}
                                        options={[
                                            { value: "", label: t("ApplicationEvents.allInstances") },
                                            ...instances.map((instance) => ({ value: instance, label: instance })),
                                        ]}
                                    />
                                </label>
                                <label>
                                    {t("ApplicationEvents.outcome")}
                                    <Select
                                        aria-label={t("ApplicationEvents.outcome")}
                                        value={filters.status}
                                        onChange={(status) => updateFilters({ ...filters, status })}
                                        options={[
                                            { value: "", label: t("ApplicationEvents.allOutcomes") },
                                            ...Object.keys(statusColors).map((status) => ({
                                                value: status,
                                                label: t(`ApplicationEvents.status.${status}`),
                                            })),
                                        ]}
                                    />
                                </label>
                                <label>
                                    {t("ApplicationEvents.from")}
                                    <Input
                                        type="datetime-local"
                                        value={filters.from}
                                        onChange={(e) => updateFilters({ ...filters, from: e.target.value })}
                                    />
                                </label>
                                <label>
                                    {t("ApplicationEvents.to")}
                                    <Input
                                        type="datetime-local"
                                        value={filters.to}
                                        onChange={(e) => updateFilters({ ...filters, to: e.target.value })}
                                    />
                                </label>
                            </div>
                            {invalidDates && (
                                <p role="alert" className={styles.DateError}>
                                    {t("ApplicationEvents.invalidDates")}
                                </p>
                            )}
                            <div className={styles.FilterActions}>
                                <Button type="link" onClick={() => updateFilters(emptyFilters)}>
                                    {t("ApplicationEvents.resetFilters")}
                                </Button>
                                <Button onClick={() => setFiltersOpen(false)}>{t("ApplicationEvents.done")}</Button>
                            </div>
                        </div>
                    }
                >
                    <Button icon={<FilterOutlined />} aria-expanded={filtersOpen}>
                        {activeFilters
                            ? t("ApplicationEvents.activeFilters", { count: activeFilters })
                            : t("ApplicationEvents.filters")}
                    </Button>
                </Popover>
                <span>{t("ApplicationEvents.timesUtc")}</span>
            </div>
            <div className={styles.FeedLayout}>
                <section className={styles.Panel}>
                    <div className={styles.PanelHeading}>
                        <h2>{t("ApplicationEvents.eventFeed")}</h2>
                        <span>{t("ApplicationEvents.newestFirst")}</span>
                    </div>
                    <Table<TPublication>
                        size="small"
                        pagination={false}
                        tableLayout="fixed"
                        scroll={{ x: 680 }}
                        dataSource={visible}
                        rowClassName={(event) =>
                            event.key === selected?.key ? `${styles.EventRow} ${styles.SelectedRow}` : styles.EventRow
                        }
                        onRow={(event) => ({ onClick: () => setSelectedKey(event.key) })}
                        locale={{ emptyText: t("ApplicationEvents.noMatches") }}
                        columns={[
                            {
                                title: t("ApplicationEvents.published"),
                                width: 120,
                                render: (_, event) => (
                                    <time
                                        className={styles.PublicationTimestamp}
                                        dateTime={new Date(event.publishedAt).toISOString()}
                                    >
                                        <code className={styles.PublicationDate}>{utcDate(event.publishedAt)}</code>
                                        <code>{utcTime(event.publishedAt)}</code>
                                    </time>
                                ),
                            },
                            {
                                title: t("ApplicationEvents.event"),
                                render: (_, event) => (
                                    <button
                                        type="button"
                                        className={styles.EventName}
                                        aria-pressed={event.key === selected?.key}
                                        onClick={() => setSelectedKey(event.key)}
                                    >
                                        <code>{event.type}</code>
                                    </button>
                                ),
                            },
                            {
                                title: t("ApplicationEvents.instance"),
                                width: 140,
                                dataIndex: "instance",
                                render: (instance) => <code className={styles.InstanceName}>{instance}</code>,
                            },
                            {
                                title: t("ApplicationEvents.outcome"),
                                width: 116,
                                render: (_, event) => <Outcome status={getStatus(event.listeners)} />,
                            },
                            {
                                title: t("ApplicationEvents.listeners"),
                                width: 124,
                                render: (_, event) =>
                                    event.listeners.length
                                        ? t("ApplicationEvents.listenerSummary", {
                                              succeeded: event.listeners.filter(
                                                  (item) => !item.error && item.durationMs !== undefined,
                                              ).length,
                                              total: event.listeners.length,
                                          })
                                        : t("ApplicationEvents.noObserved"),
                            },
                        ]}
                    />
                    <div className={styles.Footer}>
                        <span aria-live="polite">
                            {matching.length
                                ? t("ApplicationEvents.pageRange", {
                                      from: offset + 1,
                                      to: offset + visible.length,
                                      total: matching.length,
                                  })
                                : t("ApplicationEvents.zeroEvents")}
                        </span>
                        <Pagination
                            size="small"
                            current={page}
                            pageSize={PAGE_SIZE}
                            total={matching.length}
                            hideOnSinglePage
                            showSizeChanger={false}
                            onChange={setPage}
                        />
                    </div>
                </section>
                {selected && (
                    <section
                        className={`${styles.Panel} ${styles.Inspector}`}
                        aria-label={t("ApplicationEvents.eventDetails")}
                    >
                        <Outcome status={getStatus(selected.listeners)} />
                        <h2>
                            <code>{selected.type}</code>
                        </h2>
                        <EventMetadata event={selected} />
                        <h3>{t("ApplicationEvents.listeners")}</h3>
                        {selected.listeners.length ? (
                            <ul className={styles.ListenerSummary}>
                                {selected.listeners.map((listener) => (
                                    <li key={listener.method}>
                                        <code>{listener.method}</code>
                                        <Outcome status={getStatus([listener])} />
                                    </li>
                                ))}
                            </ul>
                        ) : (
                            <p>{t("ApplicationEvents.noInvocations")}</p>
                        )}
                        <Button icon={<ApartmentOutlined />} onClick={() => openPublication(selected)}>
                            {t("ApplicationEvents.openPublication")}
                        </Button>
                    </section>
                )}
            </div>
        </div>
    );
}

export default function ApplicationEvents() {
    const { instanceId } = useParams();
    return <ApplicationEventsPage key={instanceId} />;
}
