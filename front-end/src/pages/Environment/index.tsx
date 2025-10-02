import { useEffect } from "react";

import { useAppDispatch, useAppSelector } from "hooks";
import { EnvironmentProfiles } from "./EnvironmentProfiles";
import { EnvironmentTables } from "./EnvironmentTables";
import { getEnvironmentThunk } from "store/slices";
import { Loader } from "components";

export const Environment = () => {
  const dispatch = useAppDispatch();

  const { loading, error } = useAppSelector((store) => store.environment);

  useEffect(() => {
      // todo В будущем вместо hard code-а вставить динамический id.
    dispatch(getEnvironmentThunk("56019718-3b84-4ecd-9b84-287754dbd7d4"));
  }, [dispatch]);

  if (loading) {
    return <Loader />;
  }

  if (error) {
    // todo change error handling in future
    return error;
  }

  return (
    <>
      <EnvironmentProfiles />
      <EnvironmentTables />
    </>
  );
};
