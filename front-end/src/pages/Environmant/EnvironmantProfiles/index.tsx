import { Button } from "antd";
import { PlusOutlined, DeleteOutlined } from "@ant-design/icons";

import styles from "./styles.module.css";

interface IProps {
  profiles: string[];
}

export const EnvironmantProfiles = ({ profiles }: IProps) => {
  return (
    <div className={styles.MainWrapper}>
      <div className={styles.ProfilesWrapper}>
        {profiles.map((profile) => (
          <div className={styles.ProfileWrapper} key={profile}>
            <div className={styles.ProfileTitle}>Profile</div>
            <div className={styles.ProfileValue}>{profile}</div>
          </div>
        ))}
        <div className={styles.ProfilesActionButtons}>
          <Button icon={<PlusOutlined />} type="primary" />
          <Button icon={<DeleteOutlined />} type="primary" />
        </div>
      </div>
    </div>
  );
};
