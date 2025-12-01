import { ConfigProvider } from "antd";
import { StrictMode } from "react";
import { createRoot } from "react-dom/client";

import App from "./App.tsx";

import "@ant-design/v5-patch-for-react-19";
import "./index.css";

const theme = {
    token: {
        colorPrimary: "#00AB55",
        fontFamily: "'Golos', sans-serif",
        fontSize: 15,
    },
};

createRoot(document.getElementById("root")!).render(
    <StrictMode>
        <ConfigProvider
            tooltip={{
                styles: {
                    root: {
                        maxWidth: 600,
                    },
                },
            }}
            theme={theme}
        >
            <App />
        </ConfigProvider>
    </StrictMode>,
);
