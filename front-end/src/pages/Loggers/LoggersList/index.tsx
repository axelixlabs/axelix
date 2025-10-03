import { useAppSelector } from "hooks";
import { Logger } from "../Logger";

export const LoggersList = () => {
  const { levels, loggers, filteredLoggers } = useAppSelector(
    (store) => store.loggers
  );

  const loggersData = filteredLoggers.length ? filteredLoggers : loggers;

  return (
    <>
      {loggersData.map((logger) => {
        return <Logger logger={logger} levels={levels} />;
      })}
    </>
  );
};

// todo use this code in future for virtual scroll
// import { useRef } from "react";
// import { useVirtualizer } from "@tanstack/react-virtual";

// import { useAppSelector } from "hooks";
// import { Logger } from "../Logger";

// import styles from "./styles.module.css";

// export const LoggersList = () => {
//   const parentRef = useRef(null);

//   const { levels, loggers, filteredLoggers } = useAppSelector(
//     (store) => store.loggers
//   );

//   const loggersData = filteredLoggers.length ? filteredLoggers : loggers;

//   const virtualizer = useVirtualizer({
//     count: loggersData.length,
//     getScrollElement: () => parentRef.current,
//     estimateSize: () => 57,
//   });

//   return (
//     <div ref={parentRef} className={styles.MainWrapper}>
//       <div
//         style={{
//           height: `${virtualizer.getTotalSize()}px`,
//         }}
//         className={styles.InnerWrapper}
//       >
//         {virtualizer.getVirtualItems().map(({ start, index }) => {
//           const logger = loggersData[index];
//           return (
//             <div
//               key={logger.name}
//               ref={virtualizer.measureElement}
//               style={{
//                 transform: `translateY(${start}px)`,
//               }}
//               className={styles.LoggerWrapper}
//             >
//               <Logger logger={logger} levels={levels} />
//             </div>
//           );
//         })}
//       </div>
//     </div>
//   );
// };
