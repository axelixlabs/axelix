import { Provider } from "react-redux";

import { AppRoutes } from "./routes";
import { store } from "./store";
import "./i18n/i18n";

function App() {
  return (
    <Provider store={store}>
      <AppRoutes />
    </Provider>
  );
}

export default App;
