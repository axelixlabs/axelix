import { type ReactNode } from 'react';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import styles from './styles.module.css';

/**
 * Localized "Upcoming" label, keyed by Docusaurus locale. Falls back to English.
 */
const LABEL: Record<string, string> = {
    en: 'Upcoming',
    ru: 'Ожидается',
};

/**
 * Localized detail that follows the separator, keyed by locale. Falls back to English.
 */
const DETAIL: Record<string, string> = {
    en: 'Available in an upcoming minor release',
    ru: 'Появится в ближайшем минорном релизе',
};

/**
 * Metadata line that marks the heading it sits under as not-yet-released. Place it directly below
 * the heading, before the section body: `### Title` → `<UpcomingReleaseNotice />` → prose. Reads as
 * page metadata rather than decoration; the label follows the current documentation locale.
 */
export const UpcomingReleaseNotice = (): ReactNode => {
    const {
        i18n: { currentLocale },
    } = useDocusaurusContext();

    const label = LABEL[currentLocale] ?? LABEL.en;
    const detail = DETAIL[currentLocale] ?? DETAIL.en;

    return (
        <div className={styles.Notice} role="note" aria-label={`${label} — ${detail}`}>
            <span className={styles.Label}>
                <span className={styles.Dot} aria-hidden="true" />
                {label}
            </span>
            <span className={styles.Separator} aria-hidden="true">
                |
            </span>
            <span>{detail}</span>
        </div>
    );
};
