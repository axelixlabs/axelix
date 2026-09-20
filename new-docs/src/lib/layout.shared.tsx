import { uiTranslations } from "fumadocs-ui/i18n";
import type { BaseLayoutProps } from "fumadocs-ui/layouts/shared";

import { AxelixLogo } from "@/components/AxelixLogo";
import { GitHubIcon, LinkedInIcon, XIcon } from "@/components/SocialIcons";

import { LATEST_VERSION } from "./constants.mjs";
import { i18n } from "./i18n";
import { getDocsUrl, gitConfig } from "./shared";

export const translations = i18n
    .translations()
    .extend(uiTranslations())
    .add({
        en: {
            displayName: "English",
        },
        ru: {
            displayName: "Русский",

            // search
            "Search(search trigger)": "Поиск",
            "Search(search dialog)": "Поиск",
            "No results found(search dialog)": "Ничего не найдено",
            "Open Search(search trigger)(aria-label)": "Открыть поиск",
            "Close Search(search dialog)(aria-label)": "Закрыть поиск",

            // table of contents
            "On this page(table of contents)": "На этой странице",
            "No Headings(table of contents)": "Заголовков нет",
            "Table of Contents(inline table of contents)": "Содержание",

            // page footer & pagination
            "Last updated on(page footer)": "Последнее обновление",
            "Next Page(pagination)": "Следующая страница",
            "Previous Page(pagination)": "Предыдущая страница",
            "Edit on GitHub(edit page)": "Редактировать на GitHub",

            // sidebar
            "Hide Sidebar(sidebar)": "Скрыть меню",
            "Show Sidebar(sidebar)": "Показать меню",
            "Open Sidebar(sidebar)(aria-label)": "Открыть меню",
            "Close Sidebar(sidebar)(aria-label)": "Закрыть меню",
            "Collapse Sidebar(sidebar)(aria-label)": "Свернуть меню",
            "Open Sidebar(aria-label)": "Открыть меню",
            "Close Sidebar(aria-label)": "Закрыть меню",
            "Toggle Menu(home layout header)(aria-label)": "Меню",

            // language switcher
            "Choose a language(language switcher)": "Выберите язык",
            "Choose a language(language switcher)(aria-label)": "Выберите язык",

            // theme switcher
            "Toggle Theme(theme switcher)(aria-label)": "Переключить тему",
            "Light(theme switcher)(aria-label)": "Светлая",
            "Dark(theme switcher)(aria-label)": "Тёмная",
            "System(theme switcher)(aria-label)": "Системная",

            // page actions
            "Copy Markdown(page actions)": "Скопировать Markdown",
            "View as Markdown(page actions)": "Открыть как Markdown",
            "Open(page actions)": "Открыть",
            "Open in GitHub(page actions)": "Открыть в GitHub",

            // code block & headings
            "Copy Text(code block)(aria-label)": "Скопировать",
            "Copied Text(code block)(aria-label)": "Скопировано",
            "Copy Anchor Link(heading anchor)(aria-label)": "Скопировать ссылку на раздел",
        },
    });

export function baseOptions(locale: string): BaseLayoutProps {
    return {
        nav: {
            title: <AxelixLogo />,
            url: "https://axelix.io/",
        },
        // `githubUrl` is deliberately unused: it always appends GitHub *after* `links`,
        // and the design puts it first.
        links: [
            {
                url: getDocsUrl([LATEST_VERSION, "start", "getting-started"], locale),
                text: "Getting Starter",
                on: "nav",
            },
            {
                url: getDocsUrl(
                    [LATEST_VERSION, "setting-up-master-ui", "configuring-master", "configuring-master"],
                    locale,
                ),
                text: "Master",
                on: "nav",
            },
            {
                url: getDocsUrl(
                    [LATEST_VERSION, "setting-up-spring-boot-service", "spring-boot-starter", "configuration"],
                    locale,
                ),
                text: "Your Services",
                on: "nav",
            },
            {
                url: getDocsUrl([LATEST_VERSION, "more", "troubleshooting"], locale),
                text: "Troubleshooting",
                on: "nav",
            },
            {
                type: "icon",
                url: `https://github.com/${gitConfig.user}/${gitConfig.repo}`,
                text: "GitHub",
                label: "GitHub",
                icon: <GitHubIcon />,
                external: true,
            },
            { type: "icon", url: "https://x.com/axelixlabs", text: "X", label: "X", icon: <XIcon />, external: true },
            {
                type: "icon",
                url: "https://www.linkedin.com/company/133446146/",
                text: "LinkedIn",
                label: "LinkedIn",
                icon: <LinkedInIcon />,
                external: true,
            },
        ],
    };
}
