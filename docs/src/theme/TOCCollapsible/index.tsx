import React, { type ReactNode } from 'react';
import TOCCollapsible from '@theme-original/TOCCollapsible';
import type TOCCollapsibleType from '@theme/TOCCollapsible';
import type { WrapperProps } from '@docusaurus/types';
import styles from "./styles.module.css"
import { EditPost } from '@site/src/components';

type Props = WrapperProps<typeof TOCCollapsibleType>;

export default function TOCCollapsibleWrapper(props: Props): ReactNode {
  return (
    <>
      <TOCCollapsible {...props} />

      <div className={styles.MobileEdit}>
        <EditPost />
      </div>
    </>
  );
}
