import EditIcon from '@site/static/icons/edit.svg';
import styles from "./styles.module.css";
import { useDoc } from '@docusaurus/plugin-content-docs/client';

export const EditPost = () => {
    const { metadata } = useDoc();
    const { editUrl } = metadata;

    if (!editUrl) {
        return null;
    }

    return (
        <>
            <a
                href={editUrl}
                className={styles.EditLink}
                target="_blank"
                rel="noopener noreferrer"
            >
                <EditIcon /> Edit this page
            </a>
        </>
    );
};