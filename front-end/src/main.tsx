import { StrictMode } from "react";
import { ConfigProvider } from "antd";
import { createRoot } from "react-dom/client";
import "./index.css";
import App from "./App.tsx";

const theme = {
  token: {
    colorPrimary: "#00AB55",
    fontFamily: "'Golos', sans-serif",
  },
};

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <ConfigProvider theme={theme}>
      <App />
    </ConfigProvider>
  </StrictMode>
);
