/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
