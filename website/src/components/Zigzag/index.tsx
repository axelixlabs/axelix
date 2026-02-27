import { ZigzagSectionRow } from "./ZigzagSectionRow";
import { zigzagSectionsData } from "@/utils";

import styles from "./styles.module.css"

export const Zigzag = () => {
    return (
        <section className={`MainContainer ${styles.MainWrapper}`}>
            <h2 className="VisuallyHidden">ZigZag</h2>
            {zigzagSectionsData.map((section, index) => (
                <ZigzagSectionRow section={section} key={section.title} index={index} />
            ))}
        </section>
    )
} 