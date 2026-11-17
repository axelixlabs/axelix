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
    keys: [
        "Report an issue(page)",
        "Copy prompt(ai prompt)",
        "Copied!(ai prompt)",
        "Prompt copied(ai prompt)",
        "Install Axelix with AI(ai prompt)",
        "Copy prompt for your AI agent(ai prompt)(aria-label)",
    ],
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

            // AI install prompt
            "Copy prompt(ai prompt)": "Copy prompt",
            "Copied!(ai prompt)": "Copied!",
            "Prompt copied(ai prompt)": "Prompt copied",
            "Install Axelix with AI(ai prompt)": "Install Axelix with AI",
            "Copy prompt for your AI agent(ai prompt)(aria-label)": "Copy prompt for your AI agent",
        },
        de: {
            displayName: "Deutsch",

            // search
            "Search(search trigger)": "Suche",
            "Search(search dialog)": "Suche",
            "No results found(search dialog)": "Keine Ergebnisse gefunden",
            "Open Search(search trigger)(aria-label)": "Suche öffnen",
            "Close Search(search dialog)(aria-label)": "Suche schließen",

            // table of contents
            "On this page(table of contents)": "Auf dieser Seite",
            "No Headings(table of contents)": "Keine Überschriften",
            "Table of Contents(inline table of contents)": "Inhaltsverzeichnis",

            // page footer & pagination
            "Last updated on(page footer)": "Zuletzt aktualisiert am",
            "Next Page(pagination)": "Nächste Seite",
            "Previous Page(pagination)": "Vorherige Seite",
            "Edit on GitHub(edit page)": "Auf GitHub bearbeiten",

            // sidebar
            "Hide Sidebar(sidebar)": "Seitenleiste ausblenden",
            "Show Sidebar(sidebar)": "Seitenleiste einblenden",
            "Open Sidebar(sidebar)(aria-label)": "Seitenleiste öffnen",
            "Close Sidebar(sidebar)(aria-label)": "Seitenleiste schließen",
            "Collapse Sidebar(sidebar)(aria-label)": "Seitenleiste einklappen",
            "Open Sidebar(aria-label)": "Seitenleiste öffnen",
            "Close Sidebar(aria-label)": "Seitenleiste schließen",
            "Toggle Menu(home layout header)(aria-label)": "Menü umschalten",

            // language switcher
            "Choose a language(language switcher)": "Sprache auswählen",
            "Choose a language(language switcher)(aria-label)": "Sprache auswählen",

            // header breadcrumb
            "Docs(header)": "Dokumentation",

            // theme switcher
            "Toggle Theme(theme switcher)(aria-label)": "Farbschema wechseln",
            "Light(theme switcher)(aria-label)": "Hell",
            "Dark(theme switcher)(aria-label)": "Dunkel",
            "System(theme switcher)(aria-label)": "System",

            // page actions
            "Report an issue(page)": "Problem melden",
            "Copy Markdown(page actions)": "Markdown kopieren",
            "View as Markdown(page actions)": "Als Markdown anzeigen",
            "Open(page actions)": "Öffnen",
            "Open in GitHub(page actions)": "In GitHub öffnen",
            "Open in ChatGPT(page actions)": "In ChatGPT öffnen",
            "Open in Claude(page actions)": "In Claude öffnen",
            "Open in Cursor(page actions)": "In Cursor öffnen",
            "Open in Scira AI(page actions)": "In Scira AI öffnen",
            "Read {url}, I want to ask questions about it.(page actions)":
                "Lesen Sie {url}, ich möchte Fragen dazu stellen.",

            // AI install prompt
            "Copy prompt(ai prompt)": "Prompt kopieren",
            "Copied!(ai prompt)": "Kopiert!",
            "Prompt copied(ai prompt)": "Prompt kopiert",
            "Install Axelix with AI(ai prompt)": "Axelix mit KI installieren",
            "Copy prompt for your AI agent(ai prompt)(aria-label)": "Prompt für Ihren KI-Agenten kopieren",

            // code block & headings
            "Copy Text(code block)(aria-label)": "Text kopieren",
            "Copied Text(code block)(aria-label)": "Text kopiert",
            "Copy Anchor Link(heading anchor)(aria-label)": "Link zu diesem Abschnitt kopieren",
        },
        fr: {
            displayName: "Français",

            // search
            "Search(search trigger)": "Recherche",
            "Search(search dialog)": "Recherche",
            "No results found(search dialog)": "Aucun résultat trouvé",
            "Open Search(search trigger)(aria-label)": "Ouvrir la recherche",
            "Close Search(search dialog)(aria-label)": "Fermer la recherche",

            // table of contents
            "On this page(table of contents)": "Sur cette page",
            "No Headings(table of contents)": "Aucun titre",
            "Table of Contents(inline table of contents)": "Table des matières",

            // page footer & pagination
            "Last updated on(page footer)": "Dernière mise à jour",
            "Next Page(pagination)": "Page suivante",
            "Previous Page(pagination)": "Page précédente",
            "Edit on GitHub(edit page)": "Modifier sur GitHub",

            // sidebar
            "Hide Sidebar(sidebar)": "Masquer la barre latérale",
            "Show Sidebar(sidebar)": "Afficher la barre latérale",
            "Open Sidebar(sidebar)(aria-label)": "Ouvrir la barre latérale",
            "Close Sidebar(sidebar)(aria-label)": "Fermer la barre latérale",
            "Collapse Sidebar(sidebar)(aria-label)": "Réduire la barre latérale",
            "Open Sidebar(aria-label)": "Ouvrir la barre latérale",
            "Close Sidebar(aria-label)": "Fermer la barre latérale",
            "Toggle Menu(home layout header)(aria-label)": "Menu",

            // language switcher
            "Choose a language(language switcher)": "Choisissez une langue",
            "Choose a language(language switcher)(aria-label)": "Choisissez une langue",

            // header breadcrumb
            "Docs(header)": "Documentation",

            // theme switcher
            "Toggle Theme(theme switcher)(aria-label)": "Changer de thème",
            "Light(theme switcher)(aria-label)": "Clair",
            "Dark(theme switcher)(aria-label)": "Sombre",
            "System(theme switcher)(aria-label)": "Système",

            // page actions
            "Report an issue(page)": "Signaler un problème",
            "Copy Markdown(page actions)": "Copier le Markdown",
            "View as Markdown(page actions)": "Afficher en Markdown",
            "Open(page actions)": "Ouvrir",
            "Open in GitHub(page actions)": "Ouvrir dans GitHub",
            "Open in ChatGPT(page actions)": "Ouvrir dans ChatGPT",
            "Open in Claude(page actions)": "Ouvrir dans Claude",
            "Open in Cursor(page actions)": "Ouvrir dans Cursor",
            "Open in Scira AI(page actions)": "Ouvrir dans Scira AI",
            "Read {url}, I want to ask questions about it.(page actions)":
                "Lisez {url}, je souhaite poser des questions à son sujet.",

            // AI install prompt
            "Copy prompt(ai prompt)": "Copier le prompt",
            "Copied!(ai prompt)": "Copié !",
            "Prompt copied(ai prompt)": "Prompt copié",
            "Install Axelix with AI(ai prompt)": "Installer Axelix avec l’IA",
            "Copy prompt for your AI agent(ai prompt)(aria-label)": "Copier le prompt pour votre agent IA",

            // code block & headings
            "Copy Text(code block)(aria-label)": "Copier le texte",
            "Copied Text(code block)(aria-label)": "Texte copié",
            "Copy Anchor Link(heading anchor)(aria-label)": "Copier le lien vers cette section",
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

            // AI install prompt
            "Copy prompt(ai prompt)": "Скопировать",
            "Copied!(ai prompt)": "Скопировано!",
            "Prompt copied(ai prompt)": "Промпт скопирован",
            "Install Axelix with AI(ai prompt)": "Установить Axelix с ИИ",
            "Copy prompt for your AI agent(ai prompt)(aria-label)": "Скопировать промпт для ИИ-агента",

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
