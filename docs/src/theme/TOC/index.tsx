import { type ReactNode } from 'react';
import TOC from '@theme-original/TOC';
import type TOCType from '@theme/TOC';
import type { WrapperProps } from '@docusaurus/types';
import { EditPost } from '@site/src/components';
import styles from "./styles.module.css"

type Props = WrapperProps<typeof TOCType>;

export default function TOCWrapper(props: Props): ReactNode {
  return (
    <>
      <div className={styles.TOCWrapper}>
        <TOC {...props} />

        <div className={styles.TOCFooter}>
          <EditPost />
        </div>
      </div>
    </>
  );
}
