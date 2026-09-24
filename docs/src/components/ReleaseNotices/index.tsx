"use client";

import { useI18n } from "fumadocs-ui/contexts/i18n";
import type { ReactNode } from "react";

/**
 * Metadata lines that mark the heading they sit under as released, not yet released, or no longer
 * applicable. Place one directly below the heading, before the section body.
 *
 * The three share a layout — accent dot, label, separator, detail — and differ only in accent and
 * wording, so they are written as one family around `NoticeLine`.
 *
 * TODO: custom — no built-in Fumadocs notice/badge component.
 */

/** Picks the current locale's variant, falling back to English. */
const translate = (variants: Record<string, string>, locale: string | undefined) =>
    variants[locale ?? "en"] ?? variants.en;

type NoticeLineProps = {
    label: string;
    /** Rendered after the separator; carries its own version badge where there is one. */
    detail: ReactNode;
};

const NoticeLine = ({ label, detail }: NoticeLineProps) => (
    <div className="flex items-baseline gap-2.5">
        <span className="inline-flex items-center gap-1.5 font-semibold text-(--accent)">
            <span className="size-1.5 rounded-full bg-(--accent)" aria-hidden="true" />
            {label}
        </span>
        <span className="text-fd-border" aria-hidden="true">
            |
        </span>
        <span className="flex-1 leading-relaxed">{detail}</span>
    </div>
);

const Version = ({ children }: { children: ReactNode }) => (
    <span className="font-bold text-(--accent)">{children}</span>
);

/** Shared frame: accent variables, muted type, and the rule that closes the line off. */
const noticeClass = "-mt-1 mb-5 border-b border-fd-border pb-3.5 text-[0.85rem] text-fd-muted-foreground";

type VersionProps = {
    /** Release version, e.g. `1.1.0`. */
    version: string;
};

/**
 * Marks a section as available from a given release onwards. Counterpart of
 * `UpcomingReleaseNotice` — swap the two once the feature ships.
 */
export const ReleasedInNotice = ({ version }: VersionProps) => {
    const { locale } = useI18n();
    const label = translate({ en: "Released", ru: "Выпущено" }, locale);
    const detail = translate({ en: "Available since release", ru: "Доступно начиная с релиза" }, locale);

    return (
        // Green reads as "shipped", against the purple "future" of its sibling.
        <div
            className={`${noticeClass} [--accent:#2e8555] dark:[--accent:#7fd8a5]`}
            role="note"
            aria-label={`${label} — ${detail} ${version}`}
        >
            <NoticeLine
                label={label}
                detail={
                    <>
                        {detail}: <Version>{version}</Version>
                    </>
                }
            />
        </div>
    );
};

/** Marks a section as not yet released. */
export const UpcomingReleaseNotice = () => {
    const { locale } = useI18n();
    const label = translate({ en: "Upcoming", ru: "Ожидается" }, locale);
    const detail = translate(
        { en: "Available in an upcoming minor release", ru: "Появится в ближайшем минорном релизе" },
        locale,
    );

    return (
        // Purple reads as "future" without colliding with the callout colours.
        <div
            className={`${noticeClass} [--accent:#7a5cc4] dark:[--accent:#b9a9ee]`}
            role="note"
            aria-label={`${label} — ${detail}`}
        >
            <NoticeLine label={label} detail={detail} />
        </div>
    );
};

type LegacyNoticeProps = VersionProps & {
    /**
     * Why the section below is obsolete. Rendered as a muted aside hung with the line on a shared
     * left rail, so it reads as a caveat rather than the section's first paragraph. Localize it at
     * the call site — the page is already locale-specific.
     */
    reason?: ReactNode;
};

/** Marks a section as no longer applicable from a given release onwards. */
export const LegacyNotice = ({ version, reason }: LegacyNoticeProps) => {
    const { locale } = useI18n();
    const label = translate({ en: "Legacy", ru: "Устарело" }, locale);
    const detail = translate({ en: "Only needed before release", ru: "Актуально только до релиза" }, locale);
    const skip = translate(
        { en: "On later versions, skip this section.", ru: "На более поздних версиях пропустите этот раздел." },
        locale,
    );

    // Amber reads as "check whether this applies to you", between the green and the purple.
    const accent = "[--accent:#a35c12] dark:[--accent:#e0a962]";
    const line = (
        <NoticeLine
            label={label}
            detail={
                <>
                    {detail}: <Version>{version}</Version>.{reason == null && <> {skip}</>}
                </>
            }
        />
    );

    if (reason == null) {
        return (
            <div
                className={`${noticeClass} ${accent}`}
                role="note"
                aria-label={`${label} — ${detail} ${version}. ${skip}`}
            >
                {line}
            </div>
        );
    }

    // The rail replaces the bottom rule as the boundary between the caveat and the real content.
    return (
        <div
            className={`-mt-1 mb-6 grid grid-cols-[2px_1fr] gap-x-4 text-[0.85rem] text-fd-muted-foreground ${accent} [--rail:#e8c79a] dark:[--rail:#7d6234]`}
            role="note"
            aria-label={`${label} — ${detail} ${version}.`}
        >
            <span className="rounded-[1px] bg-(--rail)" aria-hidden="true" />
            <div className="flex flex-col gap-2">
                {line}
                <p className="m-0 leading-relaxed">{reason}</p>
            </div>
        </div>
    );
};
