import { useEffect } from "react";
import { useParams } from "react-router-dom";

import { useAppDispatch, useAppSelector } from "hooks";
import { EnvironmentProfiles } from "./EnvironmentProfiles";
import { EnvironmentTables } from "./EnvironmentTables";
import { getEnvironmentThunk } from "store/slices";
import { Loader } from "components";

export const Environment = () => {
  const dispatch = useAppDispatch();
  const { instanceId } = useParams();

  const { loading, error } = useAppSelector((store) => store.environment);

  useEffect(() => {
    if (instanceId) {
      dispatch(getEnvironmentThunk(instanceId));
    }
    // The dispatch passed as a dependency to useEffect does not affect its execution, since the dispatch function is never recreated.
    // There are two common approaches: either include dispatch in the dependencies or omit it. 
    // Both approaches are considered correct.
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

export default Environment;