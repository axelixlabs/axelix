import TOC from '@theme-original/TOC';
import { EditPost } from '@site/src/components';
import {ComponentProps} from "react";

import styles from "./styles.module.css"

const TOCWrapper = (props: ComponentProps<typeof TOC>) => {
    const hasToc = props.toc && props.toc.length;

    return (
        <div className={styles.TOCWrapper}>
            {!!hasToc && <TOC {...props} />}

            <div className={styles.TOCFooter}>
                <EditPost />
            </div>
        </div>
    );
}

export default TOCWrapper;