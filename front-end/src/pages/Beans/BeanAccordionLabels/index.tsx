import { TooltipWithCopy } from "components";
import type { IBean } from "models";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Single bean
     */
    bean: IBean;
}

export const BeanAccordionLabels = ({ bean }: IProps) => {
    const { beanName, className, scope, aliases } = bean;

    return (
        <div
            data-bean-entry
            data-bean-name={`${beanName}`}
            data-bean-class={`${className}`}
            data-bean-aliases={`${aliases}`}
            className={styles.AccordionHeader}
        >
            <div className={styles.AccordionHeaderContent}>
                <TooltipWithCopy text={beanName} />
                <div className={styles.ClassName}>
                    <TooltipWithCopy text={className} />
                </div>
            </div>
            <div className={styles.ScopeWrapper}>{scope}</div>
        </div>
    );
};
