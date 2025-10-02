import { useEffect } from "react";

import { useAppDispatch, useAppSelector } from "hooks";
import { EnvironmentProfiles } from "./EnvironmentProfiles";
import { EnvironmentTables } from "./EnvironmentTables";
import { environmentThunk } from "store/slices";
import { Loader } from "components";

export const Environment = () => {
  const dispatch = useAppDispatch();

  const { loading, error } = useAppSelector((store) => store.environment);

  useEffect(() => {
    dispatch(environmentThunk("1"));
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
