import { type ReactNode } from 'react';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import styles from './styles.module.css';

/**
 * Localized "Released" label, keyed by Docusaurus locale. Falls back to English.
 */
const LABEL: Record<string, string> = {
    en: 'Released',
    ru: 'Выпущено',
};

/**
 * Localized detail that follows the separator, keyed by locale. Falls back to English.
 * The version itself is rendered separately as a highlighted badge.
 */
const DETAIL: Record<string, string> = {
    en: 'Available since release',
    ru: 'Доступно начиная с релиза',
};

type ReleasedInNoticeProps = {
    /** Release version the feature shipped in, e.g. "1.1.0". */
    version: string;
};

/**
 * Metadata line that marks the heading it sits under as released in a specific version. Place it
 * directly below the heading, before the section body: `### Title` → `<ReleasedInNotice
 * version="1.1.0" />` → prose. Counterpart of `UpcomingReleaseNotice` — swap the two once the
 * feature ships; the label follows the current documentation locale.
 */
export const ReleasedInNotice = ({ version }: ReleasedInNoticeProps): ReactNode => {
    const {
        i18n: { currentLocale },
    } = useDocusaurusContext();

    const label = LABEL[currentLocale] ?? LABEL.en;
    const detail = DETAIL[currentLocale] ?? DETAIL.en;

    return (
        <div className={styles.Notice} role="note" aria-label={`${label} — ${detail} ${version}`}>
            <span className={styles.Label}>
                <span className={styles.Dot} aria-hidden="true" />
                {label}
            </span>
            <span className={styles.Separator} aria-hidden="true">
                |
            </span>
            <span>
                {detail}: <span className={styles.Version}>{version}</span>
            </span>
        </div>
    );
};
