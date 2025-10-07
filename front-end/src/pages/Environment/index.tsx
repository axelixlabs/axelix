import { message } from "antd";
import { useEffect } from "react";
import { useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";

import { EnvironmentProfiles } from "./EnvironmentProfiles";
import { EnvironmentTables } from "./EnvironmentTables";
import { useAppDispatch, useAppSelector } from "hooks";
import { getEnvironmentThunk } from "store/slices";
import { Loader } from "components";

export const Environment = () => {
  const dispatch = useAppDispatch();
  const { instanceId } = useParams();
  const { t } = useTranslation();

  const { loading, error, success } = useAppSelector((store) => store.environment);

  useEffect(() => {
    if (instanceId) {
      dispatch(getEnvironmentThunk(instanceId));
    }
  }, []);

  useEffect(() => {
    if (success) {
      message.success(t('saved'))
      // todo В будущем вместо hard code-а вставить динамический id.
      dispatch(getEnvironmentThunk("2be78791-5045-4b9a-a02a-cc5a4cdd0094"));
    }
  }, [success]);

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