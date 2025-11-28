import { Button, Checkbox, Collapse, List, Modal, Switch } from "antd";
import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";

import { Loader } from "components";
import { downloadFile, fetchData } from "helpers";
import { EExportableComponent, StatefulRequest } from "models";
import { exportStateData } from "services";

import styles from "./styles.module.css";

import DownloadIcon from "assets/icons/download.svg";

interface IProps {
    /**
     * The name of the instance
     */
    instanceName: string;
}

export const DetailsHeader = ({ instanceName }: IProps) => {
    const { instanceId } = useParams();
    const { t } = useTranslation();

    const [dataState, setDataState] = useState(StatefulRequest.loading<Blob>(false));

    const [isModalOpen, setIsModalOpen] = useState<boolean>(false);
    const [stateComponents, setStateComponents] = useState<EExportableComponent[]>([]);
    const [heapDumpExpanded, setHeapDumpExpanded] = useState<boolean>(false);
    const [sanitizeHeapDump, setSanitizeHeapDump] = useState<boolean>(true);
    const file = dataState.response;
    const loading = dataState.loading;

    useEffect(() => {
        if (heapDumpExpanded) {
            setSanitizeHeapDump(true);
        }
    }, [heapDumpExpanded]);

    useEffect(() => {
        if (file) {
            // We have to manually download the file here since the request to the server is a POST http
            // request and therefore the browser might not catch up the possible Content-Disposition header
            downloadFile(file);
        }
    }, [file]);

    const showModal = (): void => {
        setIsModalOpen(true);
    };

    const handleOk = async (): Promise<void> => {
        if (!stateComponents.length) {
            setIsModalOpen(false);
        }

        setDataState(StatefulRequest.loading());
        await fetchData(setDataState, () =>
            exportStateData({
                instanceId: instanceId!,
                body: {
                    components: stateComponents.map((value) => ({
                        component: value,
                        ...(value === EExportableComponent.HEAP_DUMP && { sanitize: sanitizeHeapDump }),
                    })),
                },
            }),
        );

        setIsModalOpen(false);
    };

    const handleCancel = (): void => {
        setIsModalOpen(false);
    };

    const handleChange = (stateComponent: EExportableComponent): void => {
        setStateComponents((prev) =>
            prev.includes(stateComponent)
                ? prev.filter((component) => component !== stateComponent)
                : [...prev, stateComponent],
        );
    };

    return (
        <div className={styles.MainWrapper}>
            <div className={styles.MainTitle}>{instanceName}</div>
            <Button
                type="primary"
                icon={<img src={DownloadIcon} alt="Download icon" className={styles.DownloadIcon} />}
                onClick={showModal}
                className={styles.Download}
            >
                {t("Details.downloadState")}
            </Button>
            <Modal
                title={t("Details.exportConfiguration")}
                cancelText={t("cancel")}
                open={isModalOpen}
                onOk={handleOk}
                onCancel={handleCancel}
                centered
                okButtonProps={{ disabled: loading }}
                cancelButtonProps={{ disabled: loading }}
            >
                {loading ? (
                    <div className={styles.LoaderWrapper}>
                        <Loader />
                    </div>
                ) : (
                    <List
                        bordered
                        dataSource={Object.values(EExportableComponent)}
                        renderItem={(component) =>
                            component !== EExportableComponent.HEAP_DUMP ? (
                                <List.Item actions={[<Switch onChange={() => handleChange(component)} />]}>
                                    {t(`Details.Components.${component}`)}
                                </List.Item>
                            ) : (
                                <Collapse
                                    expandIcon={() => false}
                                    activeKey={heapDumpExpanded ? [component] : []}
                                    items={[
                                        {
                                            key: component,
                                            label: (
                                                <div
                                                    onClick={(e) => e.stopPropagation()}
                                                    className={styles.HeapDumpAccordionHeader}
                                                >
                                                    <span className={styles.Component}>
                                                        {t(`Details.Components.${component}`)}
                                                    </span>
                                                    <Switch
                                                        checked={stateComponents.includes(component)}
                                                        onChange={(checked) => {
                                                            handleChange(component);
                                                            setHeapDumpExpanded(checked);
                                                        }}
                                                    />
                                                </div>
                                            ),
                                            children: (
                                                <div className={styles.HeapDumpAccordionBody}>
                                                    {t("Details.Components.Sanitize")}:
                                                    <div>
                                                        <Checkbox
                                                            checked={sanitizeHeapDump}
                                                            onChange={() => setSanitizeHeapDump(!sanitizeHeapDump)}
                                                        />
                                                    </div>
                                                </div>
                                            ),
                                        },
                                    ]}
                                    className={styles.Collapse}
                                />
                            )
                        }
                        className={styles.List}
                    />
                )}
            </Modal>
        </div>
    );
};
