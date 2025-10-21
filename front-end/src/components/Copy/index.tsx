import {message} from "antd";
import {useTranslation} from "react-i18next";
import {type MouseEvent} from 'react'

import styles from "./styles.module.css";

import CopyIcon from "assets/icons/copy.svg";

interface IProps {
  /**
   * Text that will be copied
   */
  text: string
}

export const Copy = ({text}: IProps) => {
  const {t} = useTranslation();

  const handleCopy = async (e: MouseEvent<HTMLImageElement>): Promise<void> => {
    e.stopPropagation();

    try {
      await navigator.clipboard.writeText(text);
      message.success(t("copied"));
    } catch {
      message.error(t("copyFailed"));
    }
  };

  return (
    <img
      src={CopyIcon}
      alt="Copy icon"
      onClick={handleCopy}
      className={styles.CopyIcon}
    />
  );
};
