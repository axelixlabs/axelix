/* TODO: Make some improvs in future*/
import styles from './styles.module.css'

export const LoggersTable = () => {
  return (
    <>
      <table>
        <thead>
          <tr>
            <th>Logger name</th>
            <th>starting point</th>
            <th>step 1</th>
            <th>step 2</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td className={styles.CellValueWithRowChunk} title="Logger name">
              com.nucleonforge.axelix
            </td>
            <td className={styles.CellValueFragmentWithIconCenter} title="starting point">
              <img
                src="/img/feature/loggers/level-info-icon.png"
                alt="Level info icon"
                className={styles.LevelIcon}
              />
            </td>
            <td className={styles.CellValueFragmentWithIconLeft} title="step 1">
              <img
                src="/img/feature/loggers/level-info-icon.png"
                alt="Level info icon"
                className={styles.LevelIcon}
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
            <td className={styles.CellValueWithRowChunk} title="Logger name">
              com.nucleonforge.axelix.sbs
            </td>
            <td className={styles.CellValueFragmentWithIconCenter} title="starting point">
              <img
                src="/img/feature/loggers/level-info-icon.png"
                alt="Level info icon"
                className={styles.LevelIcon}
              />
            </td>
            <td className={styles.CellValueFragmentWithIconLeft} title="step 1">
              <img
                src="/img/feature/loggers/configured-level-warn-icon.png"
                alt="Configured level warn icon"
                className={styles.ConfiguredLevelIcon}
              />
            </td>
            <td className={styles.CellValueFragmentWithIconLeft} title="step 2">
              <img
                src="/img/feature/loggers/level-debug-icon.png"
                alt="Level debug icon"
                className={styles.LevelIcon}
              />
            </td>
          </tr>

          <tr>
            <td className={styles.CellValueWithRowChunk} title="Logger name">
              com.nucleonforge.axile.sbs.autoconfiguration.spring
            </td>
            <td className={styles.CellValueFragmentWithIconCenter} title="starting point">
              <img
                src="/img/feature/loggers/level-info-icon.png"
                alt="Level info icon"
                className={styles.LevelIcon}
              />
            </td>
            <td className={styles.CellValueFragmentWithIconLeft} title="step 1">
              <img
                src="/img/feature/loggers/level-info-icon.png"
                alt="Level info icon"
                className={styles.LevelIcon}
              />
            </td>
            <td className={styles.CellValueFragmentWithIconLeft} title="step 2">
              <img
                src="/img/feature/loggers/level-debug-icon.png"
                alt="Level info icon"
                className={styles.LevelIcon}
              />
            </td>
          </tr>
        </tbody>
      </table>
    </>
  );
}