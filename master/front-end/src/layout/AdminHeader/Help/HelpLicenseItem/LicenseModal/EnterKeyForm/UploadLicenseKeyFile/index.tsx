import type { ChangeEvent } from "react";
import styles from "./styles.module.css"

interface IProps {
    setLicenseKey: any
}

export const UploadLicenseKeyFile = ({ setLicenseKey }: IProps) => {
    const handleFileUpload = (e: ChangeEvent<HTMLInputElement>): void => {
        const uploadedFile = e.target.files?.[0];

        if (!uploadedFile) {
            return;
        }

        const reader = new FileReader();

        reader.onload = (e) => {
            const content = e.target?.result;

            if (typeof content === "string") {
                const trimmedContent = content.trim()
                setLicenseKey(trimmedContent);
            }
        };

        reader.readAsText(uploadedFile);

        e.target.value = "";
    };

    return (
        <div>
            <span className={styles.Or}>or</span>

            <label className={styles.FileUploadLabel}>
                upload a .lic file
                <input
                    type="file"
                    accept=".lic"
                    onChange={handleFileUpload}
                    style={{ display: "none" }}
                />
            </label>
        </div>
    )
}