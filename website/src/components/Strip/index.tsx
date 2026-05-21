import styles from "./styles.module.css";

const TAPE =
  "Transactions · SQL timelines · Connection pools · Beans · Conditions · Configuration properties · Environment · Loggers · Thread dumps · Garbage collector · Caches · Scheduled tasks · MCP server";

export const Strip = () => {
  return (
    <div className={styles.MainWrapper}>
      <div className={styles.Track}>
        <div className={styles.Tape}>{TAPE}</div>
        <div className={styles.Tape}>{TAPE}</div>
      </div>
    </div>
  );
}
