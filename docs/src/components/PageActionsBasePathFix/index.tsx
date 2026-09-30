"use client";

import { FrameworkProvider } from "fumadocs-core/framework";
import type { Framework } from "fumadocs-core/framework";
import NextImage from "next/image";
import NextLink from "next/link";
import { useParams, usePathname, useRouter } from "next/navigation";
import type { ReactNode } from "react";

import { BASE_PATH } from "@/lib/constants.mjs";

/**
 * TODO: wrapper — Fumadocs' page-action popover (Open in ChatGPT/Claude/...) builds its
 * prompt from `usePathname()`, which Next.js documents as excluding `basePath`. This
 * overrides the framework context for just that subtree so the prompt points an AI
 * reader at the page's Markdown rendition (`{path}.md`, see `proxy.ts`) instead of
 * the HTML page — cleaner text, no nav/sidebar noise, fewer tokens.
 */
export function PageActionsBasePathFix({ children }: { children: ReactNode }) {
    const pathname = usePathname();

    return (
        <FrameworkProvider
            Link={NextLink as Framework["Link"]}
            Image={NextImage as Framework["Image"]}
            useRouter={useRouter}
            useParams={useParams}
            usePathname={() => `${BASE_PATH}${pathname}.md`}
        >
            {children}
        </FrameworkProvider>
    );
}
