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
        zh: {
            displayName: "简体中文",

            // search
            "Search(search trigger)": "搜索",
            "Search(search dialog)": "搜索",
            "No results found(search dialog)": "未找到结果",
            "Open Search(search trigger)(aria-label)": "打开搜索",
            "Close Search(search dialog)(aria-label)": "关闭搜索",

            // table of contents
            "On this page(table of contents)": "本页内容",
            "No Headings(table of contents)": "暂无标题",
            "Table of Contents(inline table of contents)": "目录",

            // page footer & pagination
            "Last updated on(page footer)": "最后更新于",
            "Next Page(pagination)": "下一页",
            "Previous Page(pagination)": "上一页",
            "Edit on GitHub(edit page)": "在 GitHub 上编辑",

            // sidebar
            "Hide Sidebar(sidebar)": "隐藏侧边栏",
            "Show Sidebar(sidebar)": "显示侧边栏",
            "Open Sidebar(sidebar)(aria-label)": "打开侧边栏",
            "Close Sidebar(sidebar)(aria-label)": "关闭侧边栏",
            "Collapse Sidebar(sidebar)(aria-label)": "收起侧边栏",
            "Open Sidebar(aria-label)": "打开侧边栏",
            "Close Sidebar(aria-label)": "关闭侧边栏",
            "Toggle Menu(home layout header)(aria-label)": "菜单",

            // language switcher
            "Choose a language(language switcher)": "选择语言",
            "Choose a language(language switcher)(aria-label)": "选择语言",

            // header breadcrumb
            "Docs(header)": "文档",

            // theme switcher
            "Toggle Theme(theme switcher)(aria-label)": "切换主题",
            "Light(theme switcher)(aria-label)": "浅色",
            "Dark(theme switcher)(aria-label)": "深色",
            "System(theme switcher)(aria-label)": "跟随系统",

            // page actions
            "Report an issue(page)": "报告问题",
            "Copy Markdown(page actions)": "复制 Markdown",
            "View as Markdown(page actions)": "以 Markdown 查看",
            "Open(page actions)": "打开",
            "Open in GitHub(page actions)": "在 GitHub 中打开",
            "Open in ChatGPT(page actions)": "在 ChatGPT 中打开",
            "Open in Claude(page actions)": "在 Claude 中打开",
            "Open in Cursor(page actions)": "在 Cursor 中打开",
            "Open in Scira AI(page actions)": "在 Scira AI 中打开",
            "Read {url}, I want to ask questions about it.(page actions)": "请阅读 {url}，我想询问与之相关的问题。",

            // AI install prompt
            "Copy prompt(ai prompt)": "复制提示词",
            "Copied!(ai prompt)": "已复制！",
            "Prompt copied(ai prompt)": "提示词已复制",
            "Install Axelix with AI(ai prompt)": "使用 AI 安装 Axelix",
            "Copy prompt for your AI agent(ai prompt)(aria-label)": "复制适用于 AI 智能体的提示词",

            // code block & headings
            "Copy Text(code block)(aria-label)": "复制",
            "Copied Text(code block)(aria-label)": "已复制",
            "Copy Anchor Link(heading anchor)(aria-label)": "复制本节链接",
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
