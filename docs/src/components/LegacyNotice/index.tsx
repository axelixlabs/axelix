import { type ReactNode } from 'react';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import styles from './styles.module.css';

/**
 * Localized "Legacy" label, keyed by Docusaurus locale. Falls back to English.
 */
const LABEL: Record<string, string> = {
    en: 'Legacy',
    ru: 'Устарело',
};

/**
 * Localized detail that precedes the version, keyed by locale. Falls back to English.
 * The version itself is rendered separately as a highlighted badge.
 */
const DETAIL: Record<string, string> = {
    en: 'Only needed before release',
    ru: 'Актуально только до релиза',
};

/**
 * Localized skip sentence that follows the version, keyed by locale. Falls back to English.
 */
const SKIP: Record<string, string> = {
    en: 'On later versions, skip this section.',
    ru: 'На более поздних версиях пропустите этот раздел.',
};

type LegacyNoticeProps = {
    /** Release version starting from which the section no longer applies, e.g. "1.2.0". */
    version: string;
};

/**
 * Metadata line that marks the heading it sits under as no longer applicable from a specific
 * version onwards. Place it directly below the heading, before the section body: `### Title` →
 * `<LegacyNotice version="1.2.0" />` → prose. Third state of the `ReleasedInNotice` /
 * `UpcomingReleaseNotice` family; the label follows the current documentation locale.
 */
export const LegacyNotice = ({ version }: LegacyNoticeProps): ReactNode => {
    const {
        i18n: { currentLocale },
    } = useDocusaurusContext();

    const label = LABEL[currentLocale] ?? LABEL.en;
    const detail = DETAIL[currentLocale] ?? DETAIL.en;
    const skip = SKIP[currentLocale] ?? SKIP.en;

    return (
        <div
            className={styles.Notice}
            role="note"
            aria-label={`${label} — ${detail} ${version}. ${skip}`}
        >
            <span className={styles.Label}>
                <span className={styles.Dot} aria-hidden="true" />
                {label}
            </span>
            <span className={styles.Separator} aria-hidden="true">
                |
            </span>
            <span className={styles.Detail}>
                {detail}: <span className={styles.Version}>{version}</span>. {skip}
            </span>
        </div>
    );
};
