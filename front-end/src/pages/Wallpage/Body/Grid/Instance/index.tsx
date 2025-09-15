import {
  InstanceState,
  type ApplicationInstance,
} from "../../../../../types/ApplicationInstance";
import "./Instance.css";
import classNames from "classnames";

export const Instance = ({ instance }: { instance: ApplicationInstance }) => {
  return (
    <>
      <div className={classNames(" grid_instance", getStyling(instance.state))}>
        <span className="instance_name">{instance.name}</span>
        <br />
        <div className="instance_meta_info">
          Commit: {instance.commitHash}
          <br />
          Ver: {instance.version}
          <br />
        </div>
      </div>
    </>
  );
};

function getStyling(state: InstanceState) {
  switch (state) {
    case InstanceState.DOWN:
      return "grid_instance_unhealthy";
    case InstanceState.UP:
      return "grid_instance_healthy";
    case InstanceState.UNKNOWN:
      return "grid_instance_unknown";
  }
}
