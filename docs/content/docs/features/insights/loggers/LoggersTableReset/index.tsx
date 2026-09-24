/* TODO: Make some improvs in future*/
import { BASE_PATH } from "@/lib/constants.mjs";

import styles from "../styles.module.css";

export const LoggersTableReset = () => {
    const configuredLevelDebugIcon = `${BASE_PATH}/img/feature/loggers/configured-level-debug-icon.png`;
    const configuredLevelTraceIcon = `${BASE_PATH}/img/feature/loggers/configured-level-trace-icon.png`;
    const configuredLevelInfoIcon = `${BASE_PATH}/img/feature/loggers/configured-level-info-icon.png`;

    return (
        <>
            <table className={styles.LoggersTable}>
                <thead>
                    <tr>
                        <th>Logger name</th>
                        <th>Starting point</th>
                        <th>Step 1</th>
                        <th>Step 2</th>
                    </tr>
                </thead>

                <tbody>
                    <tr>
                        <td>com.axelixlabs.axelix</td>

                        <td>
                            <img src={configuredLevelDebugIcon} alt="Configured level debug icon" />
                        </td>

                        <td>
                            <img src={configuredLevelDebugIcon} alt="Configured level debug icon" />
                        </td>

                        <td>
                            <img src={configuredLevelDebugIcon} alt="Configured level debug icon" />
                        </td>
                    </tr>

                    <tr>
                        <td>com.axelixlabs.axelix.sbs</td>

                        <td>
                            <img src={configuredLevelTraceIcon} alt="Configured level trace icon" />
                        </td>

                        <td>
                            <img src={configuredLevelInfoIcon} alt="Configured level info icon" />
                        </td>

                        <td>
                            <img src={configuredLevelTraceIcon} alt="Configured level trace icon" />
                        </td>
                    </tr>
                </tbody>
            </table>
        </>
    );
};
