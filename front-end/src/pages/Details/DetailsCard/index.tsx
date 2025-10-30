import { Copy } from "components";
import { resolveIconFromContent } from "helpers";
import { detailsIcons, isCopyNeeded } from "utils";

import styles from "./styles.module.css";

interface IProps {
    title: any;
    content: any;
}

export const DetailsCard = ({ title, content }: IProps) => {
    const icon = detailsIcons[resolveIconFromContent(title, content) || title];

    return (
        <div className={`CustomizedAntdTable ${styles.Card}`} key={title}>
            <div className="TableHeader">
                <div className={`RowChunk ${styles.TableHeaderRowChunk}`}>
                    {icon && <img src={icon} alt={`${title} icon`} className={styles.CardIcon} />}
                    {title}
                </div>
            </div>

            {content.map(([title, value]) => (
                <div className="TableRow" key={title}>
                    <div className="RowChunk">{title}</div>
                    <div className="RowChunk">
                        <div className={styles.ValueWrapper}>
                            {value}
                            {isCopyNeeded.includes(title) && <Copy text={value} />}
                        </div>
                    </div>
                </div>
            ))}
        </div>
    );
};
