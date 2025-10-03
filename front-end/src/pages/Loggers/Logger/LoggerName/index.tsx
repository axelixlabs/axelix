import { message, Tooltip } from "antd";
import { useTranslation } from "react-i18next";
import { CopyOutlined } from "@ant-design/icons";

import styles from "./styles.module.css";

interface IProps {
  /**
   * Single logger name
   */
  name: string;
}

export const LoggerName = ({ name }: IProps) => {
  const { t } = useTranslation();

  const handleCopy = (copyText: string): void => {
    navigator.clipboard.writeText(copyText);
    message.success(t("copied"));
  };

  return (
    <div className={styles.TruncatWrapper}>
      <Tooltip
        title={name}
        styles={{
          root: {
            maxWidth: 600,
            whiteSpace: "normal",
          },
        }}
        className={styles.Truncat}
      >
        {name}
      </Tooltip>
      <CopyOutlined
        onClick={() => handleCopy(name)}
        className={styles.CopyIcon}
      />
    </div>
  );
};
