import { Empty } from "antd";
import type { PropsWithChildren } from "react";
import { useTranslation } from "react-i18next";

interface IProps {
    /**
     * If no data is found after the searches, isEmpty will be true
     */
    isEmpty: boolean;
}

export const EmptyHandler = ({ isEmpty, children }: PropsWithChildren<IProps>) => {
    const { t } = useTranslation();

    if (isEmpty) {
        return <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description={<p>{t("noData")}</p>} />;
    }

    return children;
};
