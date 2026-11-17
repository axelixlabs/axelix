import styles from "./styles.module.css";

type Line = {
    version: string;
    released: string;
    /** Release date of the next minor. Absent while the line is the latest one. */
    supportEnds?: string;
};

/**
 * Minor lines shown on the Support Policy page, newest first. Update on every minor release:
 * set `supportEnds` of the previous line and add the new one on top.
 */
const LINES: Line[] = [
    { version: "1.2", released: "2026-10-10" },
    { version: "1.1", released: "2026-09-10", supportEnds: "2026-10-10" },
    { version: "1.0", released: "2026-07-29", supportEnds: "2026-09-10" },
];

/** The expected gap between two minor releases, used to draw the open end of the latest line. */
const EXPECTED_CADENCE_DAYS = 45;
const DAY_MS = 24 * 60 * 60 * 1000;

const LABELS = {
    de: { supported: "OSS-Support", expected: "Bis zum nächsten Minor-Release (erwartet)", ended: "Beendet" },
    en: { supported: "OSS support", expected: "Until the next minor (expected)", ended: "Ended" },
    ru: { supported: "Поддержка OSS", expected: "До следующего минора (ожидается)", ended: "Завершена" },
    es: { supported: "Soporte OSS", expected: "Hasta la próxima versión menor (previsto)", ended: "Finalizado" },
    tr: { supported: "OSS desteği", expected: "Bir sonraki minör sürüme kadar (beklenen)", ended: "Sona erdi" },
    fr: { supported: "Support OSS", expected: "Jusqu’à la prochaine version mineure (prévision)", ended: "Terminé" },
};

const toTime = (date: string) => new Date(`${date}T00:00:00Z`).getTime();

/**
 * Spring-style Gantt chart of the OSS support window of each Axelix minor line.
 */
export const SupportTimeline = ({ locale = "en" }: { locale?: "en" | "ru" | "fr" | "de" | "es" | "tr" }) => {
    const labels = LABELS[locale];
    const bars = LINES.map((line) => {
        const start = toTime(line.released);
        const end = line.supportEnds ? toTime(line.supportEnds) : start + EXPECTED_CADENCE_DAYS * DAY_MS;
        return { ...line, start, end, open: !line.supportEnds };
    });

    const first = new Date(Math.min(...bars.map((bar) => bar.start)));
    const last = new Date(Math.max(...bars.map((bar) => bar.end)));
    const months: Date[] = [];
    for (
        let month = new Date(Date.UTC(first.getUTCFullYear(), first.getUTCMonth(), 1));
        month <= last;
        month = new Date(Date.UTC(month.getUTCFullYear(), month.getUTCMonth() + 1, 1))
    ) {
        months.push(month);
    }
    const axisStart = months[0].getTime();
    const axisEnd = Date.UTC(last.getUTCFullYear(), last.getUTCMonth() + 1, 1);
    const percent = (time: number) => ((time - axisStart) / (axisEnd - axisStart)) * 100;
    const monthFormat = new Intl.DateTimeFormat(locale, { month: "short", year: "2-digit", timeZone: "UTC" });

    return (
        <figure className={`${styles.Timeline} not-prose`}>
            <div className={styles.Grid}>
                <div />
                <div className={styles.Axis}>
                    {months.map((month) => (
                        <span key={month.getTime()} style={{ left: `${percent(month.getTime())}%` }}>
                            {monthFormat.format(month)}
                        </span>
                    ))}
                </div>
                {bars.map((bar) => (
                    <div key={bar.version} className={styles.Row}>
                        <div className={styles.Version}>{bar.version}.x</div>
                        <div className={styles.Track}>
                            {months.map((month) => (
                                <i
                                    key={month.getTime()}
                                    className={styles.Tick}
                                    style={{ left: `${percent(month.getTime())}%` }}
                                />
                            ))}
                            <div
                                className={`${styles.Bar} ${bar.open ? styles.Open : styles.Ended}`}
                                style={{
                                    left: `${percent(bar.start)}%`,
                                    width: `${percent(bar.end) - percent(bar.start)}%`,
                                }}
                                title={bar.open ? labels.expected : `${labels.supported}: ${labels.ended}`}
                            />
                        </div>
                    </div>
                ))}
            </div>
            <figcaption className={styles.Legend}>
                <span>
                    <i className={`${styles.Swatch} ${styles.Ended}`} />
                    {labels.supported}: {labels.ended.toLowerCase()}
                </span>
                <span>
                    <i className={`${styles.Swatch} ${styles.Open}`} />
                    {labels.supported}: {labels.expected.charAt(0).toLowerCase() + labels.expected.slice(1)}
                </span>
            </figcaption>
        </figure>
    );
};
