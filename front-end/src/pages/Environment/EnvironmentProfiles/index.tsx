import { useState } from "react";
import { Button, Input, Modal } from "antd";
import { useTranslation } from "react-i18next";
import { PlusOutlined, DeleteFilled } from "@ant-design/icons";

import styles from "./styles.module.css";

interface IProps {
  /**
   * List of profiles that are currently active in the Spring Boot application
   */
  activeProfiles: string[];
}

export const EnvironmentProfiles = ({ activeProfiles }: IProps) => {
  const { t } = useTranslation();

  const [isModalOpen, setIsModalOpen] = useState(false);

  const showModal = () => {
    setIsModalOpen(true);
  };

  const handleOk = () => {
    // todo in future write logic for saving active profile data
    setIsModalOpen(false);
  };

  const handleCancel = () => {
    setIsModalOpen(false);
  };

  return (
    <div className={styles.MainWrapper}>
      <div className={styles.ProfilesWrapper}>
        {activeProfiles.map((activeProfile) => (
          <div className={styles.ProfileWrapper} key={activeProfile}>
            <div className={styles.ProfileTitle}>Profile</div>
            <div className={styles.ProfileValue}>
              {activeProfile}
              <DeleteFilled className={styles.DeleteActiveProfileIcon} />
            </div>
          </div>
        ))}
        <Button icon={<PlusOutlined />} type="primary" onClick={showModal} />
        <Modal
          title={t("addActiveProfile")}
          open={isModalOpen}
          onOk={handleOk}
          onCancel={handleCancel}
          okText={t("save")}
          cancelText={t("cancel")}
        >
          <Input />
        </Modal>
      </div>
    </div>
  );
};
