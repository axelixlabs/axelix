import { useEffect } from "react";

import { useAppDispatch, useAppSelector } from "../../hooks";
import { EnvironmantProfiles } from "./EnvironmantProfiles";
import { EnvironmantTables } from "./EnvironmantTables";
import { environmentThunk } from "../../store/slices";

export const Environmant = () => {
  const dispatch = useAppDispatch();

  const { data, loading, error } = useAppSelector((store) => store.environmant);

  useEffect(() => {
    dispatch(environmentThunk("1"));
  }, [dispatch]);

  if (loading) {
    return "Loading...";
  }

  if (error) {
    return error;
  }

  return (
    <>
      <EnvironmantProfiles profiles={data.activeProfiles} />
      <EnvironmantTables propertySources={data.propertySources} />
    </>
  );
};
