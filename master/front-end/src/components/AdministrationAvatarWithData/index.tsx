import { getInitials } from "@/helpers"
import styles from "./styles.module.css";
import { Avatar, type AvatarProps } from "antd";

interface IProps {
    primaryText: string;
    secondaryText?: string;
    size?: AvatarProps['size'];
    primaryTextStyles?: string;
}

export const AdministrationAvatarWithData = ({ primaryText, secondaryText, primaryTextStyles, size }: IProps) => {
    return (
        <>
            <div className={styles.MainWrapper}>
                <Avatar size={size}>{getInitials(primaryText)}</Avatar>
                <div>
                    <div className={`${styles.PrimaryText} ${primaryTextStyles ? primaryTextStyles : ""}`}>{primaryText}</div>
                    <div className={`TextSmall ${styles.SecondaryText}`}>{secondaryText}</div>
                </div>
            </div>
        </>

    )
}