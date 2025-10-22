import { Provider } from "react-redux";

import { store } from "store";

import { AppRoutes } from "./routes";

import "./i18n/i18n";

function App() {
    return (
        <Provider store={store}>
            <AppRoutes />
        </Provider>
    );
}

export default App;
