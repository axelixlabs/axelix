import { ProxyType } from "models";
import styles from "../styles.module.css";
import { useTranslation } from "react-i18next";

interface IProps {

  /**
   * The proxying algorithm used to create the instance of the bean. Might be null
   * in case the backend was unable to figure it out.
   */
  proxyType: ProxyType | null
}

function resolveProxying(t: (msg: string) => string, proxyType: ProxyType | null): string {

  if (!proxyType) {
    return t("unknownProxyingType")
  }

  let message: string

  switch (proxyType) {
    case ProxyType.CGLIB: {
      message = t("cglibProxy");
      break;
    }
    case ProxyType.JDK_PROXY: {
      message = t("jdkProxy");
      break;
    }
    case ProxyType.NO_PROXYING: {
      message = t("noProxy");
      break;
    }
  }

  return message;
}


export const BeanProxyType = ({ proxyType } : IProps ) => {

  const { t } = useTranslation();

  return (
    <>
      <div className={styles.CollapseBodyChunkTitle}>{t("beanProxyType")}:</div>
      <div>
        <span>{resolveProxying(t, proxyType)}</span>
      </div>
    </>
  );
}