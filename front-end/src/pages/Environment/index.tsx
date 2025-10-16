import { message } from "antd";
import { useEffect } from "react";
import { useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";

import { EnvironmentProfiles } from "./EnvironmentProfiles";
import { resetChangePropertySuccess } from "store/slices";
import { EnvironmentTables } from "./EnvironmentTables";
import { useAppDispatch, useAppSelector } from "hooks";
import { getEnvironmentThunk } from "store/thunks";
import { Loader } from "components";

export const Environment = () => {
  const { t } = useTranslation();
  const dispatch = useAppDispatch();
  const { instanceId } = useParams();

  const { loading, error } = useAppSelector((store) => store.environment);
  const { changePropertySuccess } = useAppSelector((store) => store.updateProperty);

  const fetchEnvironment = () => {
    if (instanceId) {
      dispatch(getEnvironmentThunk(instanceId));
    }
  };

  // todo So far, I haven't been able to find a way to combine the useEffects without causing an extra server request.
  useEffect(() => {
    fetchEnvironment()
  }, []);

  useEffect(() => {
    if (changePropertySuccess) {
      fetchEnvironment();
      message.success(t("propertyChangedSuccessfully"));
      dispatch(resetChangePropertySuccess());
    }
  }, [changePropertySuccess]);

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