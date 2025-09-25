import { useState } from "react";
import { Button, Input } from "antd";
import { useTranslation } from "react-i18next";
import { PlusOutlined, DeleteFilled, CheckOutlined } from "@ant-design/icons";

import styles from "./styles.module.css";

interface IProps {
  /**
   * List of profiles that are currently active in the Spring Boot application
   */
  activeProfiles: string[];
}

export const EnvironmentProfiles = ({ activeProfiles }: IProps) => {
  const { t } = useTranslation();

  const [createProfile, setCreateProfile] = useState(false);

  return (
    <div className={styles.MainWrapper}>
      <div className={styles.ProfilesWrapper}>
        <div className={styles.ProfileTitle}>{t("activeProfiles")}</div>
        {activeProfiles.map((activeProfile) => (
          <div className={styles.ProfileWrapper} key={activeProfile}>
            <div className={styles.ProfileValue}>
              {activeProfile}
              <DeleteFilled className={styles.DeleteActiveProfileIcon} />
            </div>
          </div>
        ))}
        <div>
          {createProfile ? (
            <div className={styles.CreateProfileWrapper}>
              <Input className={styles.CreateProfileInput} />
              <Button
                icon={<CheckOutlined />}
                type="primary"
                className={styles.CreateProfileActionsButton}
              />
            </div>
          ) : (
            <Button
              icon={<PlusOutlined />}
              type="primary"
              onClick={() => setCreateProfile(true)}
            />
          )}
        </div>
      </div>
    </div>
  );
};
