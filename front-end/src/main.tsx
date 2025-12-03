import { ConfigProvider } from "antd";
import { StrictMode } from "react";
import { createRoot } from "react-dom/client";

import App from "./App.tsx";

import "./index.css";

const theme = {
    token: {
        colorPrimary: "#00AB55",
        fontFamily: "'Golos', sans-serif",
        fontSize: 15,
    },
    components: { Tooltip: { zIndexPopup: 500, maxWidth: 600 } },
};

createRoot(document.getElementById("root")!).render(
    <StrictMode>
        <ConfigProvider theme={theme}>
            <App />
        </ConfigProvider>
    </StrictMode>,
);
