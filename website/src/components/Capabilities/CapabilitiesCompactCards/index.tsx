import {
    CachesIcon,
    ComposeIcon,
    ConditionsIcon,
    EnvironmentIcon,
    ExportIcon,
    GcIcon,
    InstanceIcon,
    TasksIcon,
} from "@/assets";

import { AccessBadge } from "../AccessBadge";
import sharedStyles from "../shared.module.css";

import styles from "./styles.module.css";

const cards = [
    {
        title: "Environment",
        description: "Every env source the JVM sees, ranked by precedence — with selective masking.",
        icon: <EnvironmentIcon width="16" height="16" />,
        href: "https://axelix.io/docs/features/properties",
    },
    {
        title: "Caches",
        description: "Cache managers, hit/miss rates, evict and clear — from a button or a tool call.",
        icon: <CachesIcon width="16" height="16" />,
        href: "https://axelix.io/docs/features/caches",
    },
    {
        title: "Thread dump",
        description: "Threads, states, locks and contention — on demand, no kill-switch required.",
        icon: <ComposeIcon width="16" height="16" />,
        href: "https://axelix.io/docs/features/thread-dump",
    },
    {
        title: "Garbage collector",
        description: "Live GC events, pause times and tuning signals — not just a chart, a feed.",
        icon: <GcIcon width="16" height="16" />,
        href: "https://axelix.io/docs/features/garbage-collector",
    },
    {
        title: "Scheduled tasks",
        description: "Inspect cron and fixed-delay tasks; toggle, force-run, or rewrite expressions live.",
        icon: <TasksIcon width="16" height="16" />,
        href: "https://axelix.io/docs/features/scheduled-tasks",
    },
    {
        title: "Conditions",
        description: "Auto-configuration matched and not-matched, with the reason for every verdict.",
        icon: <ConditionsIcon width="16" height="16" />,
        href: "https://axelix.io/docs/features/conditions",
    },
    {
        title: "Instance details",
        description: "Service metadata, build info, runtime version — the boring stuff you need at 3 AM.",
        icon: <InstanceIcon width="16" height="16" />,
        href: "https://axelix.io/docs/features/details",
    },
    {
        title: "Diagnostic export",
        description: "One-click bundle of dumps, beans, configs and conditions — for incidents and audits.",
        icon: <ExportIcon width="16" height="16" />,
        href: "https://axelix.io/docs/features/details#download-state-components",
    },
];

export const CapabilitiesCompactCards = () => {
    return (
        <div className={styles.MainWrapper}>
            {cards.map(({ title, description, icon, href }) => (
                <a rel="noopener noreferrer" target="_blank" href={href} key={title}>
                    <article className={sharedStyles.Card}>
                        <header className={sharedStyles.CardHeader}>
                            <div className={styles.Icon}>{icon}</div>
                            <AccessBadge />
                        </header>
                        <h4 className={styles.Title}>{title}</h4>
                        <p className={styles.Description}>{description}</p>
                    </article>
                </a>
            ))}
        </div>
    );
};
