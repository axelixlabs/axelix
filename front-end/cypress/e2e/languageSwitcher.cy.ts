interface ITranslationItem {
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

  cy.window()
    .its("localStorage")
    .invoke("getItem", "i18nextLng")
    .should("eq", lang);
};

describe("Internationalization works correctly", () => {
  beforeEach(() => {
    cy.window().then((win) => {
      win.localStorage.setItem("accessToken", "mockAccessToken");
    });
    cy.visit("/");
  });

  it("Should have default language - Russian", () => {
    cy.window()
      .its("localStorage")
      .invoke("getItem", "i18nextLng")
      .should("eq", "ru");

    findTranslatedElements("ru");
  });

  it("Should switch language correctly", () => {
    switchLanguage("en");

    findTranslatedElements("en");

    switchLanguage("ru");

    findTranslatedElements("ru");
  });
});
