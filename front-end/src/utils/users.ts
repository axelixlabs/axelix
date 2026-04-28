import type {UserProfileEditableValueField} from "models";
import type { InputProps } from "antd";

export const userProfileInputTypeMap: Record<UserProfileEditableValueField, InputProps["type"]> = {
    username: "text",
    email: "email",
    password: "password",
};