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
import i18n from "i18next";
import LanguageDetector from "i18next-browser-languagedetector";
import { initReactI18next } from "react-i18next";

import en from "./locales/en.json";
import ru from "./locales/ru.json";

i18n.use(LanguageDetector)
    .use(initReactI18next)
    .init({
        fallbackLng: "en",
        resources: {
            ru: {
                translation: ru,
            },
            en: {
                translation: en,
            },
        },
        detection: {
            order: ["localStorage"],
            caches: ["localStorage"],
        },
        interpolation: {
            escapeValue: false,
        },
    });

export default i18n;
