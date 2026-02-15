/* TODO: Make some improvs in future*/
import styles from './styles.module.css'

export const LoggersTableReset = () => {
  return (
    <>
      <table>
        <thead>
          <tr>
            <th>Loggers name</th>
            <th>starting point</th>
            <th>step 1</th>
            <th>step 2</th>
          </tr>
        </thead>
        <tbody>

          <tr>
            <td className={styles.CellValueWithRowChunk} title="Loggers name">
              com.nucleonforge.axelix
            </td>
            <td className={styles.CellValueFragmentWithIconCenter} title="starting point">
              <img
                src="/img/feature/loggers/configured-level-debug-icon.png"
                alt="Configured level debug icon"
                className={styles.ConfiguredLevelIcon}
              />
            </td>
            <td className={styles.CellValueFragmentWithIconLeft} title="step 1">
              <img
                src="/img/feature/loggers/configured-level-debug-icon.png"
                alt="Configured level debug icon"
                className={styles.ConfiguredLevelIcon}
              />
            </td>
            <td className={styles.CellValueFragmentWithIconLeft} title="step 2">
              <img
                src="/img/feature/loggers/configured-level-debug-icon.png"
                alt="Configured level debug icon"
                className={styles.ConfiguredLevelIcon}
              />
            </td>
          </tr>

          <tr>
            <td className={styles.CellValueWithRowChunk} title="Loggers name">
              com.nucleonforge.axelix.sbs
            </td>
            <td className={styles.CellValueFragmentWithIconCenter} title="starting point">
              <img
                src="/img/feature/loggers/configured-level-trace-icon.png"
                alt="Configured level trace icon"
                className={styles.ConfiguredLevelIcon}
              />
            </td>
            <td className={styles.CellValueFragmentWithIconLeft} title="step 1">
              <img
                src="/img/feature/loggers/configured-level-info-icon.png"
                alt="Configured level info icon"
                className={styles.ConfiguredLevelIcon}
              />
            </td>
            <td className={styles.CellValueFragmentWithIconLeft} title="step 2">
              <img
                src="/img/feature/loggers/configured-level-trace-icon.png"
                alt="Configured level trace icon"
                className={styles.ConfiguredLevelIcon}
              />
            </td>
          </tr>
        </tbody>
      </table>
    </>
  );
}