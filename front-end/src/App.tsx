import { useState } from "react";

import {
  emptyFilter,
  type GridFilters,
} from "./pages/Wallpage/Body/Grid/GridFilters";
import { Body } from "./pages/Wallpage/Body";
import { Header } from "./pages/Wallpage/Header";

function App() {
  const [filter, setFilter] = useState<GridFilters>(emptyFilter());

  return (
    <>
      <Header
        gridFilterProps={{
          filter: filter,
          filterSetter: setFilter,
        }}
      />
      <div className="MainWrapper">
        <Body filter={filter} />
      </div>
    </>
  );
}

export default App;
