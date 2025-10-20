import { message } from "antd";
import {useEffect, useState} from "react";
import { useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";

import { EnvironmentProfiles } from "./EnvironmentProfiles";
import { EnvironmentTables } from "./EnvironmentTables";
import { useAppSelector } from "hooks";
import { Loader } from "components";
import {type IEnvironmentData, StatefulRequest} from "models";
import {fetchData} from "helpers";
import {getEnvironmentData} from "services";

export const Environment = () => {
  const { t } = useTranslation();
  const { instanceId } = useParams();

  const [environment, setEnvironment] = useState(StatefulRequest.loading<IEnvironmentData>());

  const updateProperty = useAppSelector((store) => store.updateProperty);

  const fetchEnvironment = (instanceId: string) => fetchData(
    setEnvironment,
    () => getEnvironmentData(instanceId));

  // todo So far, I haven't been able to find a way to combine the useEffects without causing an extra server request.
  useEffect(() => {
    if (instanceId) {
      fetchEnvironment(instanceId);
    }
  }, []);

  useEffect(() => {
    if (updateProperty.completedSuccessfully() && instanceId) {
      fetchEnvironment(instanceId);
      message.success(t("propertyChangedSuccessfully"));
    }
  }, [updateProperty]);

  if (environment.loading) {
    return <Loader />;
  }

  if (environment.error) {
    // todo change error handling in future
    return environment.error;
  }

  return (
    <>
      <EnvironmentProfiles activeProfiles={environment.response!.activeProfiles} />
      <EnvironmentTables propertySources={environment.response!.propertySources} />
    </>
  );
};

export default Environment;