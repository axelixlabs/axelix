import { Dispatch, SetStateAction } from "react";
import styles from "./styles.module.css"

interface IProps {
    step: 1 | 2 | 3;
    setStep: Dispatch<SetStateAction<1 | 2 | 3>>;
    STEP_NAMES: any
}

export const InstallerBoardFooter = ({ step, setStep, STEP_NAMES }: IProps) => {
    return (
        <div className={styles.MainWrapper}>
            <button
                className={styles.NavButton}
                type="button"
                disabled={step <= 1}
                onClick={() => {
                    if (step > 1) {
                        setStep((step - 1) as 1 | 2 | 3)
                    }
                }}
            >
                ← {step > 1 ? STEP_NAMES[(step - 1) as 1 | 2 | 3] : "Previous"}
            </button>
            <span className={styles.Status}>Step {step} of 3</span>
            <button
                className={`${styles.NavButton} ${styles.Next}`}
                type="button"
                disabled={step === 3}
                onClick={() => {
                    setStep((step + 1) as 1 | 2 | 3)
                }}
            >
                Next: {STEP_NAMES[(step + 1) as 1 | 2 | 3]} →
            </button>
        </div>
    )
} 