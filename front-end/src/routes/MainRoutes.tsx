import { Navigate, Route, Routes } from "react-router-dom";

export const MainRoutes = () => {
  return (
    <Routes>
      <Route path="/" element={<>1</>} />
      <Route path="*" element={<Navigate to="/" />} />
    </Routes>
  );
};

// this part of code is needed in future

// import {
//   emptyFilter,
//   type GridFilters,
// } from "./pages/Wallpage/Body/Grid/GridFilters";
// import { Body } from "./pages/Wallpage/Body";
// import { Header } from "./pages/Wallpage/Header";

// const [filter, setFilter] = useState<GridFilters>(emptyFilter());
{
  /* <Header
        gridFilterProps={{
          filter: filter,
          filterSetter: setFilter,
        }}
      />
      <div className="MainWrapper">
        <Body filter={filter} />
      </div> */
}
