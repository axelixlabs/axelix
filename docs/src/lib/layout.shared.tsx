import { uiTranslations } from "fumadocs-ui/i18n";
import type { BaseLayoutProps } from "fumadocs-ui/layouts/shared";

import { AxelixLogo } from "@/components/AxelixLogo";
import { GitHubIcon, LinkedInIcon, XIcon } from "@/components/SocialIcons";

import { i18n } from "./i18n";
import { GITHUB_REPO_URL } from "./shared";

// TODO: wrapper — extends Fumadocs' uiTranslations() with our own header/page translation keys.
/** Same shape `uiTranslations()` registers its own keys with — see `fumadocs-ui/i18n`. */
const headerTranslations = {
    keys: ["Docs(header)"],
} satisfies { keys: string[] };

const pageActionsTranslations = {
    keys: ["Report an issue(page)"],
} satisfies { keys: string[] };

export const translations = i18n
    .translations()
    .extend(uiTranslations())
    .extend(headerTranslations)
    .extend(pageActionsTranslations)
    .add({
        en: {
            displayName: "English",

            // header breadcrumb
            "Docs(header)": "Docs",

            // page actions
            "Report an issue(page)": "Report an issue",
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

            // header breadcrumb
            "Docs(header)": "Документация",

            // theme switcher
            "Toggle Theme(theme switcher)(aria-label)": "Переключить тему",
            "Light(theme switcher)(aria-label)": "Светлая",
            "Dark(theme switcher)(aria-label)": "Тёмная",
            "System(theme switcher)(aria-label)": "Системная",

            // page actions
            "Report an issue(page)": "Сообщить о проблеме",
            "Copy Markdown(page actions)": "Скопировать Markdown",
            "View as Markdown(page actions)": "Открыть как Markdown",
            "Open(page actions)": "Открыть",
            "Open in GitHub(page actions)": "Открыть в GitHub",
            "Open in ChatGPT(page actions)": "Открыть в ChatGPT",
            "Open in Claude(page actions)": "Открыть в Claude",
            "Open in Cursor(page actions)": "Открыть в Cursor",
            "Open in Scira AI(page actions)": "Открыть в Scira AI",
            "Read {url}, I want to ask questions about it.(page actions)":
                "Прочитай {url}, я хочу задать по нему вопросы.",

            // code block & headings
            "Copy Text(code block)(aria-label)": "Скопировать",
            "Copied Text(code block)(aria-label)": "Скопировано",
            "Copy Anchor Link(heading anchor)(aria-label)": "Скопировать ссылку на раздел",
        },
    });

export function baseOptions(): BaseLayoutProps {
    return {
        nav: {
            title: <AxelixLogo />,
            url: "https://axelix.io/",
        },
        // `githubUrl` is deliberately unused: it always appends GitHub *after* `links`,
        // and the design puts it first.
        links: [
            {
                type: "icon",
                url: GITHUB_REPO_URL,
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
