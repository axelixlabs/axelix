/* TODO: Make some improvs in future*/
import useBaseUrl from '@docusaurus/useBaseUrl';
import styles from '../styles.module.css'

export const LoggersTable = () => {
    const levelInfoIcon = useBaseUrl('/img/feature/loggers/level-info-icon.png');
    const levelDebugIcon = useBaseUrl('/img/feature/loggers/level-debug-icon.png');
    const configuredLevelDebugIcon = useBaseUrl(
        '/img/feature/loggers/configured-level-debug-icon.png'
    );
    const configuredLevelWarnIcon = useBaseUrl(
        '/img/feature/loggers/configured-level-warn-icon.png'
    );

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
                    <td>
                        com.axelixlabs.axelix
                    </td>

                    <td>
                        <img
                            src={levelInfoIcon}
                            alt="Level info icon"
                        />
                    </td>

                    <td>
                        <img
                            src={levelInfoIcon}
                            alt="Level info icon"
                        />
                    </td>

                    <td>
                        <img
                            src={configuredLevelDebugIcon}
                            alt="Configured level debug icon"
                        />
                    </td>
                </tr>

                <tr>
                    <td>
                        com.axelixlabs.axelix.sbs
                    </td>

                    <td>
                        <img
                            src={levelInfoIcon}
                            alt="Level info icon"
                        />
                    </td>

                    <td>
                        <img
                            src={configuredLevelWarnIcon}
                            alt="Configured level warn icon"
                        />
                    </td>

                    <td>
                        <img
                            src={levelDebugIcon}
                            alt="Level debug icon"
                        />
                    </td>
                </tr>

                <tr>
                    <td>
                        com.axelixlabs.axelix.sbs.autoconfiguration.spring
                    </td>

                    <td>
                        <img
                            src={levelInfoIcon}
                            alt="Level info icon"
                        />
                    </td>

                    <td>
                        <img
                            src={levelInfoIcon}
                            alt="Level info icon"
                        />
                    </td>

                    <td>
                        <img
                            src={levelDebugIcon}
                            alt="Level debug icon"
                        />
                    </td>
                </tr>
                </tbody>
            </table>
        </>
    );
};