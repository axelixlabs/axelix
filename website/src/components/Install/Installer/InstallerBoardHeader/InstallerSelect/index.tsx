import { CheckIcon, ChevronDownIcon } from "@/assets";

import styles from "./styles.module.css";

export interface IProps {
    label: string;
    open: boolean;
    onToggle: () => void;
    options: { id: string; label: string }[];
    active: string;
    onPick: (id: string) => void;
}

export const InstallerSelect = ({ label, open, onToggle, options, active, onPick }: IProps) => {
    return (
        <div className={`${styles.MainWrapper} ${open ? styles.ActiveWrapper : ""}`}>
            <button
                className={styles.Trigger}
                type="button"
                onClick={(e) => {
                    e.stopPropagation();
                    onToggle();
                }}
            >
                <span className={styles.Label}>{label}</span>
                <ChevronDownIcon className={styles.Arrow} />
            </button>
            <div className={styles.Menu}>
                {options.map(({ id, label }) => (
                    <button
                        key={id}
                        className={`${styles.Option} ${active === id ? styles.ActiveOption : ""}`}
                        type="button"
                        onClick={() => {
                            onPick(id);
                        }}
                    >
                        {label}
                        <CheckIcon className={styles.CheckIcon} />
                    </button>
                ))}
            </div>
        </div>
    );
};
