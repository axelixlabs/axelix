"use client";

import { useTranslations } from "@fuma-translate/react";

import { EditOnGitHub } from "fumadocs-ui/layouts/notebook/page";
import { Flag } from "lucide-react";

const ACTION_LINK =
    "flex items-center gap-1.5 text-sm text-fd-muted-foreground transition-colors hover:text-fd-foreground";

type TocPageActionsProps = {
    editUrl: string;
    reportUrl: string;
};

export const TocPageActions = ({ editUrl, reportUrl }: TocPageActionsProps) => {
    const t = useTranslations();

    return (
        <div className="mt-4 flex -translate-y-[6px] translate-x-[21px] flex-col gap-2 pt-4 pb-[7px]">
            {/* TODO: wrapper — restyles Fumadocs' EditOnGitHub as a flat TOC action. */}
            <EditOnGitHub
                href={editUrl}
                className={`${ACTION_LINK} justify-start rounded-none border-0 bg-transparent p-0 font-normal`}
            />

            {/* TODO: custom — Fumadocs has no native action for reporting documentation issues. */}
            <a href={reportUrl} target="_blank" rel="noreferrer noopener" className={ACTION_LINK}>
                <Flag className="size-3.5" />
                {t("Report an issue", { note: "page" })}
            </a>
        </div>
    );
};
