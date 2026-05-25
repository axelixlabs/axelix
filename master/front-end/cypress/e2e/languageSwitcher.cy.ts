/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
interface ITranslationItem {
    // todo В будущем определить где будут содержаться интерфейси тестов

    /**
     * Data used to find specific elements on a page and check whether they have been translated.
     * Depending on the selected language, an attempt will be made to find the translatable text.
     */
    locator: string;
    ruText: string;
    enText: string;
}

const translationItems: ITranslationItem[] = [
    {
        locator: '[data-test="header-links"]',
        ruText: "Дашборд",
        enText: "Dashboard",
    },
    {
        locator: '[data-test="header-links"]',
        ruText: "Панель",
        enText: "Wallboard",
    },
    {
        locator: ".ant-layout-sider-children",
        ruText: "Аналитика",
        enText: "Insights",
    },
    {
        locator: ".ant-layout-sider-children",
        ruText: "Логгеры",
        enText: "Loggers",
    },
    {
        locator: ".ant-layout-sider-children",
        ruText: "Сопоставления",
        enText: "Mappings",
    },
];

const findTranslatedElements = (lang: "ru" | "en"): void => {
    translationItems.forEach(({ locator, ruText, enText }) => {
        const currentTranslates = lang === "ru" ? ruText : enText;
        cy.contains(locator, currentTranslates).should("exist");
    });
};

const switchLanguage = (lang: "ru" | "en"): void => {
    const currentLanguage = lang === "ru" ? "Русский" : "English";
    cy.get('[data-test="language-switcher-select"]').click();
    cy.contains(".ant-select-item", currentLanguage).click();

    cy.window().its("localStorage").invoke("getItem", "i18nextLng").should("eq", lang);
};

describe("Internationalization works correctly", () => {
    beforeEach(() => {
        cy.window().then((win) => {
            win.localStorage.setItem("accessToken", "mockAccessToken");
        });
        cy.visit("/");
    });

    it("Should have default language - Russian", () => {
        cy.window().its("localStorage").invoke("getItem", "i18nextLng").should("eq", "ru");

        findTranslatedElements("ru");
    });

    it("Should switch language correctly", () => {
        switchLanguage("en");

        findTranslatedElements("en");

        switchLanguage("ru");

        findTranslatedElements("ru");
    });
});
