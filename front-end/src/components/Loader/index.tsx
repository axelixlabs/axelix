import { Spin } from "antd";
import { LoadingOutlined } from "@ant-design/icons";

import styles from "./styles.module.css";

export const Loader = () => {
  return (
    <div className={styles.MainWrapper}>
      <Spin indicator={<LoadingOutlined style={{ fontSize: 100 }} spin />} />
    </div>
  );
};
