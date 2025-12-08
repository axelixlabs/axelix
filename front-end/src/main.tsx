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
import { ConfigProvider } from "antd";
import { StrictMode } from "react";
import { createRoot } from "react-dom/client";

import App from "./App.tsx";

import "./index.css";
import "./antdCustomizedTable.css";

const theme = {
    token: {
        colorPrimary: "#00ab55",
        fontFamily: "'Golos', sans-serif",
        fontSize: 15,
    },
};

createRoot(document.getElementById("root")!).render(
    <StrictMode>
        <ConfigProvider
            theme={theme}
            tooltip={{
                styles: {
                    root: { maxWidth: "600px", whiteSpace: "normal" },
                },
            }}
        >
            <App />
        </ConfigProvider>
    </StrictMode>,
);
