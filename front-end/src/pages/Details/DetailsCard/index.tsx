import { Copy } from "components";
import { resolveIconFromContent } from "helpers";
import type { ECopyableField } from "models";
import { detailsIcons, isCopyNeeded } from "utils";

import styles from "./styles.module.css";

interface IProps {
    title: string;
    content: string[][];
}

export const DetailsCard = ({ title, content }: IProps) => {
    // @ts-expect-error Fix this in future
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
                            {isCopyNeeded.includes(title as ECopyableField) && <Copy text={value} />}
                        </div>
                    </div>
                </div>
            ))}
        </div>
    );
};
