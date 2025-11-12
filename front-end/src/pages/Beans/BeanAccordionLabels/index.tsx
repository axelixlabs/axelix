import { Link, useParams } from "react-router-dom";

import { TooltipWithCopy } from "components";
import { canonicalize } from "helpers";
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
                        <Link
                            to={`/instance/${instanceId}/config-props#${canonicalize(beanName)}`}
                            onClick={(e) => e.stopPropagation()}
                        >
                            <img src={LinkIcon} alt="Link icon" />
                        </Link>
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
