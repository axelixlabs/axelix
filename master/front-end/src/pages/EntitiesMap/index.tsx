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
import { SearchOutlined } from "@ant-design/icons";

import { Input, Select } from "antd";
import { type CSSProperties, type PropsWithChildren, useEffect, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router";

import { EmptyHandler, Loader } from "@/components";
import {
    ENTITY_CHECK_LABELS,
    type IEntityFinding,
    type TEntityFindingLocation,
    fetchData,
    getEntityFindings,
} from "@/helpers";
import { type IMappedEntity, type IPersistenceInsights, StatefulRequest } from "@/models";
import { getTransactionalData } from "@/services";

import styles from "./styles.module.css";

const EntitiesMap = () => {
    const { instanceId } = useParams();

    return instanceId ? <EntitiesMapContent key={instanceId} instanceId={instanceId} /> : <EmptyHandler isEmpty />;
};

interface IProps {
    /** The instance whose JPA mappings are displayed. */
    instanceId: string;
}

const EntitiesMapContent = ({ instanceId }: IProps) => {
    const [insights, setInsights] = useState(StatefulRequest.loading<IPersistenceInsights>());

    useEffect(() => {
        fetchData(setInsights, () => getTransactionalData(instanceId));
    }, [instanceId]);

    if (insights.loading) {
        return <Loader />;
    }

    const entities = insights.response?.entitiesMap?.entities ?? [];
    if (insights.error || entities.length === 0) {
        return <EmptyHandler isEmpty />;
    }

    return <EntityBrowser entities={entities} />;
};

interface IEntityBrowserProps {
    /** All mapped entities reported for the instance. */
    entities: IMappedEntity[];
}

const EntityBrowser = ({ entities }: IEntityBrowserProps) => {
    const { t } = useTranslation();
    const [search, setSearch] = useState("");
    const [check, setCheck] = useState("");
    const [location, setLocation] = useState<TEntityFindingLocation | "">("");
    const [selectedName, setSelectedName] = useState("");

    const query = search.trim().toLowerCase();
    const inventory = entities.map((entity) => ({ entity, findings: getEntityFindings(entity) }));
    const availableChecks = new Set(inventory.flatMap(({ findings }) => findings.map((finding) => finding.check)));
    const checkLabel = (id: string) => {
        const label = ENTITY_CHECK_LABELS.get(id);
        return label ? t(`EntitiesMap.problems.${label}`) : id;
    };
    const checkOptions = [...new Set([...ENTITY_CHECK_LABELS.keys(), ...availableChecks])].map((id) => ({
        value: id,
        label: checkLabel(id),
    }));

    const results = inventory
        .filter(({ entity }) => [entity.name, entity.table].some((field) => field.toLowerCase().includes(query)))
        .map(({ entity, findings }) => ({
            entity,
            findings: findings.filter(
                (finding) => (!check || finding.check === check) && (!location || finding.location === location),
            ),
        }))
        .filter(({ findings }) => (!check && !location) || findings.length > 0);

    const withFindings = results.filter(({ findings }) => findings.length > 0);
    const withoutFindings = results.filter(({ findings }) => findings.length === 0);
    const selected =
        results.find(({ entity }) => entity.name === selectedName) ?? withFindings[0] ?? withoutFindings[0];
    const findingCount = withFindings.reduce((total, result) => total + result.findings.length, 0);
    const findingsLabel = t("EntitiesMap.findingCount", { count: findingCount });

    const renderEntityGroup = (label: string, group: typeof results) => (
        <details className={styles.EntityGroup} hidden={group.length === 0} open>
            <summary className={styles.GroupLabel}>
                {label} · {group.length}
            </summary>
            <ul>
                {group.map(({ entity, findings }) => (
                    <li key={entity.name}>
                        <button
                            type="button"
                            className={styles.Entity}
                            aria-pressed={selected?.entity === entity}
                            onClick={() => setSelectedName(entity.name)}
                        >
                            <span>
                                <strong>{entity.name}</strong>
                                <code className={styles.Table}>{entity.table}</code>
                            </span>
                            {findings.length > 0 && (
                                <span
                                    className={styles.Count}
                                    aria-label={t("EntitiesMap.findingCount", { count: findings.length })}
                                >
                                    {findings.length}
                                </span>
                            )}
                        </button>
                    </li>
                ))}
            </ul>
        </details>
    );

    return (
        <div className={styles.MainWrapper}>
            <header>
                <h1 className="TextLarge">{t("Sider.entitiesMap")}</h1>
                <p className={styles.Description}>{t("EntitiesMap.description")}</p>
            </header>

            <div className={styles.Toolbar}>
                <Select
                    aria-label={t("EntitiesMap.checkFilter")}
                    className={styles.CheckSelect}
                    showSearch={{ optionFilterProp: "label" }}
                    value={check}
                    onChange={setCheck}
                    options={[{ value: "", label: t("EntitiesMap.allChecks") }, ...checkOptions]}
                />
                <Select
                    aria-label={t("EntitiesMap.locationFilter")}
                    className={styles.LocationSelect}
                    value={location}
                    onChange={setLocation}
                    options={[
                        { value: "", label: t("EntitiesMap.allLocations") },
                        { value: "association", label: t("EntitiesMap.locations.associations") },
                        { value: "attribute", label: t("EntitiesMap.locations.attributes") },
                    ]}
                />
                <p className={styles.Summary} role="status">
                    {check || location || query
                        ? t("EntitiesMap.filteredSummary", {
                              findings: findingsLabel,
                              entities: t("EntitiesMap.entityCount", { count: results.length }),
                          })
                        : t("EntitiesMap.summary", {
                              findings: findingsLabel,
                              matching: withFindings.length,
                              total: entities.length,
                              count: entities.length,
                          })}
                </p>
            </div>

            <div className={styles.Split}>
                <EntityListScroll>
                    <div className={styles.PanelHeader}>
                        <h2 id="entities-map-list">{t("EntitiesMap.entities")}</h2>
                        <Input
                            className={styles.Search}
                            prefix={<SearchOutlined />}
                            allowClear
                            value={search}
                            onChange={(event) => setSearch(event.target.value)}
                            aria-label={t("EntitiesMap.search")}
                            placeholder={t("EntitiesMap.search")}
                        />
                    </div>
                    {renderEntityGroup(t("EntitiesMap.withFindings"), withFindings)}
                    {renderEntityGroup(t("EntitiesMap.withoutFindings"), withoutFindings)}
                    {results.length === 0 && <p className={styles.PanelHeader}>{t("EntitiesMap.noMatches")}</p>}
                </EntityListScroll>

                {selected && (
                    <EntityDetail key={selected.entity.name} entity={selected.entity} findings={selected.findings} />
                )}
            </div>
        </div>
    );
};

const EntityListScroll = ({ children }: PropsWithChildren) => {
    const viewport = useRef<HTMLDivElement>(null);
    const content = useRef<HTMLDivElement>(null);
    const [metrics, setMetrics] = useState({ maxScroll: 0, thumbHeight: 0 });
    const [scrollTop, setScrollTop] = useState(0);

    useEffect(() => {
        const scroller = viewport.current;
        const list = content.current;

        if (!scroller || !list) {
            return;
        }

        const observer = new ResizeObserver(() => {
            const { clientHeight, scrollHeight, scrollTop: currentTop } = scroller;
            setMetrics({
                maxScroll: scrollHeight - clientHeight,
                thumbHeight: (clientHeight / Math.max(scrollHeight, 1)) * clientHeight,
            });
            setScrollTop(currentTop);
        });

        observer.observe(scroller);
        observer.observe(list);
        return () => observer.disconnect();
    }, []);

    return (
        <section className={`${styles.Panel} ${styles.EntityList}`} aria-labelledby="entities-map-list">
            <div
                id="entities-map-scroll"
                ref={viewport}
                className={styles.EntityListViewport}
                onScroll={(event) => setScrollTop(event.currentTarget.scrollTop)}
            >
                <div ref={content}>{children}</div>
            </div>
            {metrics.maxScroll > 0 && (
                <input
                    type="range"
                    aria-labelledby="entities-map-list"
                    aria-controls="entities-map-scroll"
                    aria-orientation="vertical"
                    className={styles.EntityListScrollbar}
                    style={{ "--thumb-height": `${metrics.thumbHeight}px` } as CSSProperties}
                    min={0}
                    max={metrics.maxScroll}
                    step="any"
                    value={scrollTop}
                    onChange={(event) => {
                        const top = event.currentTarget.valueAsNumber;
                        viewport.current?.scrollTo({ top, behavior: "instant" });
                        setScrollTop(top);
                    }}
                />
            )}
        </section>
    );
};

interface IEntityDetailProps {
    /** The selected entity. */
    entity: IMappedEntity;
    /** Findings that match the active filters. */
    findings: IEntityFinding[];
}

const EntityDetail = ({ entity, findings }: IEntityDetailProps) => {
    const { t } = useTranslation();

    return (
        <section className={styles.Panel} aria-labelledby="entities-map-entity">
            <header className={styles.PanelHeader}>
                <h2 id="entities-map-entity" className={styles.EntityTitle}>
                    {entity.name}
                </h2>
                <p className={styles.Metadata}>
                    <code>{entity.table}</code>
                    {typeof entity.associationsCount === "number" && (
                        <>
                            {" "}
                            ·{" "}
                            {t("EntitiesMap.associationCount", {
                                count: entity.associationsCount,
                            })}
                        </>
                    )}
                </p>
            </header>

            <div className={styles.Findings}>
                <h3>
                    {t("EntitiesMap.findings")} · {findings.length}
                </h3>
                {findings.length === 0 && <p>{t("EntitiesMap.noFindings")}</p>}
                {findings.map(({ check: id, location: kind, member, annotation }, index) => {
                    const label = ENTITY_CHECK_LABELS.get(id);

                    const key = JSON.stringify([id, kind, member]);
                    const heading = (
                        <span>
                            <strong>{label ? t(`EntitiesMap.problems.${label}`) : id}</strong>
                            <span className={styles.Location}>
                                {t(`EntitiesMap.locations.${kind}`)} <code>{member}</code>
                            </span>
                        </span>
                    );

                    if (!label) {
                        return (
                            <div className={styles.Finding} key={key}>
                                <div className={styles.FindingHeading}>{heading}</div>
                            </div>
                        );
                    }

                    return (
                        <details className={styles.Finding} key={key} open={index === 0}>
                            <summary className={styles.FindingHeading}>{heading}</summary>
                            <div className={styles.FindingBody}>
                                {annotation && <code>{annotation}</code>}
                                <p>{t(`EntitiesMap.problemFixes.${label}`)}</p>
                            </div>
                        </details>
                    );
                })}
            </div>
        </section>
    );
};

export default EntitiesMap;
