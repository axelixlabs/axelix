import { StrictMode } from "react";
import { ConfigProvider } from "antd";
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
    <ConfigProvider theme={theme}>
      <App />
    </ConfigProvider>
  </StrictMode>
);
