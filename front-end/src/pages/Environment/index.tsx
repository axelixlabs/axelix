import { message } from "antd";
import { useEffect } from "react";
import { useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";

import { getEnvironmentThunk, resetChangePropertySuccess } from "store/slices";
import { EnvironmentProfiles } from "./EnvironmentProfiles";
import { EnvironmentTables } from "./EnvironmentTables";
import { useAppDispatch, useAppSelector } from "hooks";
import { Loader } from "components";

export const Environment = () => {
  const { t } = useTranslation();
  const dispatch = useAppDispatch();
  const { instanceId } = useParams();
  const [messageApi, contextHolder] = message.useMessage();

  const { loading, error } = useAppSelector((store) => store.environment);
  const { changePropertySuccess } = useAppSelector((store) => store.updateProperty);

  useEffect(() => {
    if (instanceId) {
      dispatch(getEnvironmentThunk(instanceId));
    }
  }, []);

  useEffect(() => {
    if (changePropertySuccess) {
      messageApi.success(t('saved'))
      dispatch(resetChangePropertySuccess())
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
      {contextHolder}
      <EnvironmentProfiles />
      <EnvironmentTables />
    </>
  );
};

export default Environment;