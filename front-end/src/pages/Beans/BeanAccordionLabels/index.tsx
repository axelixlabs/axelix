import { Tooltip } from "antd";
import { useTranslation } from "react-i18next";
import { Link, useParams } from "react-router-dom";

import { TooltipWithCopy } from "components";
import { normalizeHtmlElementId } from "helpers";
import type { IBean } from "models";

import styles from "./styles.module.css";

import LinkIcon from "assets/icons/link.svg";

interface IProps {
    /**
     * Single bean
     */
    bean: IBean;
}

export const BeanAccordionLabels = ({ bean }: IProps) => {
    const { beanName, className, scope, aliases, isConfigPropsBean } = bean;
    const { instanceId } = useParams();

    const { t } = useTranslation();

    return (
        <div
            data-bean-entry
            data-bean-name={beanName}
            data-bean-class={className}
            data-bean-aliases={aliases}
            className={styles.AccordionHeader}
        >
            <div className={styles.AccordionHeaderContent}>
                <div className={styles.BeanNameWrapper}>
                    <TooltipWithCopy text={beanName} />
                    {isConfigPropsBean && (
                        <Tooltip title={t("configPropsBeanLink")}>
                            <Link
                                to={`/instance/${instanceId}/config-props#${normalizeHtmlElementId(beanName)}`}
                                onClick={(e) => e.stopPropagation()}
                            >
                                <img src={LinkIcon} alt="Link icon" />
                            </Link>
                        </Tooltip>
                    )}
                </div>
                <div className={styles.ClassName}>
                    <TooltipWithCopy text={className} />
                </div>
            </div>
            <div className={styles.ScopeWrapper}>{scope}</div>
        </div>
    );
};
