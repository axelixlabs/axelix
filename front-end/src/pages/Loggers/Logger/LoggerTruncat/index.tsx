import { message, Tooltip } from "antd";
import { CopyOutlined } from "@ant-design/icons";

import styles from "./styles.module.css";
import { useTranslation } from "react-i18next";
import type { ILogger } from "models";

interface IProps {
  /**
   * Single logger
   */
  logger: ILogger;
}

export const LoggerTruncat = ({ logger }: IProps) => {
  const { t } = useTranslation();

  const handleCopy = (copyText: string): void => {
    navigator.clipboard.writeText(copyText);
    message.success(t("copied"));
  };

  return (
    <div className={styles.TruncatWrapper}>
      <Tooltip
        title={logger.name}
        styles={{
          root: {
            maxWidth: 600,
            whiteSpace: "normal",
          },
        }}
        className={styles.Truncat}
      >
        {logger.name}
      </Tooltip>
      <CopyOutlined
        onClick={() => handleCopy(logger.name)}
        className={styles.CopyIcon}
      />
    </div>
  );
};
