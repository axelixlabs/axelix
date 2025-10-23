import { Select } from "antd";
import { useTranslation } from "react-i18next";

import styles from "./styles.module.css";

export const LanguageSwitcher = () => {
    const { i18n } = useTranslation();

    const handleChange = (value: string): void => {
        i18n.changeLanguage(value);
    };

  return (
    <Select
      defaultValue={i18n.language}
      onChange={handleChange}
      options={[
        { value: "en", label: "English" },
        { value: "ru", label: "Русский" },
      ]}
      className={styles.LanguageSwitcherSelect}
    />
  );
};
