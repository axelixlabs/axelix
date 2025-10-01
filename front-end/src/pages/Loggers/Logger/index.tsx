import { ConfigProvider, Radio } from "antd";
import { useTranslation } from "react-i18next";

import { setLoggerLevelThunk } from "store/slices";
import { LoggerTruncat } from "./LoggerTruncat";
import { useAppDispatch } from "hooks";
import type { ILogger } from "models";
import { statePalette } from "utils";

import styles from "./styles.module.css";

interface IProps {
  /**
   * Levels for all loggers
   */
  levels: string[];
  /**
   * Single logger
   */
  logger: ILogger;
}

export const Logger = ({ levels, logger }: IProps) => {
  const { t } = useTranslation();
  const dispatch = useAppDispatch();

  const radioGroupColor = logger.configuredLevel
    ? // @ts-expect-error todo fix types in future
      statePalette[logger.configuredLevel]
    : // @ts-expect-error todo fix types in future
      statePalette[`${logger.effectiveLevel}Light`];

  return (
    <div className={styles.MainWrapper}>
      <LoggerTruncat logger={logger} />

      <div className={styles.LoggerValuesWrapper}>
        <ConfigProvider
          theme={{
            components: {
              Radio: radioGroupColor,
            },
          }}
        >
          <Radio.Group
            defaultValue={logger.effectiveLevel}
            buttonStyle="solid"
            onChange={() => dispatch(setLoggerLevelThunk(""))}
            className={styles.RadioGroup}
          >
            {levels.map((level) => (
              <Radio.Button value={level} key={level}>
                {level}
              </Radio.Button>
            ))}
          </Radio.Group>
        </ConfigProvider>
        <button className={styles.Reset}>{t("reset")}</button>
      </div>
    </div>
  );
};

// todo delete in the future if not needed

// import { useState } from "react";
// import { ConfigProvider, Radio } from "antd";
// import { useTranslation } from "react-i18next";

// import { LoggerTruncat } from "./LoggerTruncat";
// import type { ILogger } from "models";

// import styles from "./styles.module.css";
// import { useAppDispatch } from "hooks";
// import { setLoggerLevelThunk } from "store/slices";

// interface IProps {
//   levels: string[];
//   logger: ILogger;
// }

// export const Logger = ({ levels, logger }: IProps) => {
//   const { t } = useTranslation();
//   const dispatch = useAppDispatch();

//   const [selectedLevel, setSelectedLevel] = useState<string>("");

//   const statePalette: Record<string, Record<string, string>> = {
//     OFF: {
//       colorPrimary: "#000000",
//       colorPrimaryHover: "#1a1a1a",
//       colorPrimaryActive: "#333333",
//     },
//     OFFLight: {
//       colorPrimary: "#4d4d4d",
//       colorPrimaryHover: "#666666",
//       colorPrimaryActive: "#808080",
//     },
//     ERROR: {
//       colorPrimary: "#ff4d4f",
//       colorPrimaryHover: "#ff7875",
//       colorPrimaryActive: "#d9363e",
//     },
//     ERRORLight: {
//       colorPrimary: "#ff999a",
//       colorPrimaryHover: "#ffb3b4",
//       colorPrimaryActive: "#ff8081",
//     },
//     WARN: {
//       colorPrimary: "#faad14",
//       colorPrimaryHover: "#ffd666",
//       colorPrimaryActive: "#d48806",
//     },
//     WARNLight: {
//       colorPrimary: "#ffe08c",
//       colorPrimaryHover: "#fff2b3",
//       colorPrimaryActive: "#ffd34d",
//     },
//     INFO: {
//       colorPrimary: "#1890ff",
//       colorPrimaryHover: "#69c0ff",
//       colorPrimaryActive: "#096dd9",
//     },
//     INFOLight: {
//       colorPrimary: "#a3d0ff",
//       colorPrimaryHover: "#cce6ff",
//       colorPrimaryActive: "#70b8ff",
//     },
//     DEBUG: {
//       colorPrimary: "#52c41a",
//       colorPrimaryHover: "#95de64",
//       colorPrimaryActive: "#389e0d",
//     },
//     DEBUGLight: {
//       colorPrimary: "#b7eb8f",
//       colorPrimaryHover: "#dcf5b6",
//       colorPrimaryActive: "#85c441",
//     },
//     TRACE: {
//       colorPrimary: "#722ed1",
//       colorPrimaryHover: "#b37feb",
//       colorPrimaryActive: "#531dab",
//     },
//     TRACELight: {
//       colorPrimary: "#c6a9f3",
//       colorPrimaryHover: "#e3d0fb",
//       colorPrimaryActive: "#a077e0",
//     },
//   };

//   return (
//     <div className={styles.LoggerWrapper}>
//       <LoggerTruncat logger={logger} />

//       <div className={styles.LoggerValuesWrapper}>
//         <ConfigProvider
//           theme={{
//             components: {
//               Radio: selectedLevel
//                 ? statePalette[selectedLevel]
//                 : logger.configuredLevel
//                 ? statePalette[logger.configuredLevel]
//                 : statePalette[`${logger.effectiveLevel}Light`],
//             },
//           }}
//         >
//           <Radio.Group
//             defaultValue={logger.effectiveLevel}
//             buttonStyle="solid"
//             onChange={(e) => {
//               setSelectedLevel(e.target.value);
//               dispatch(setLoggerLevelThunk(""));
//             }}
//             className={styles.RadioGroup}
//           >
//             {levels.map((level) => (
//               <Radio.Button value={level} key={level}>
//                 {level}
//               </Radio.Button>
//             ))}
//           </Radio.Group>
//         </ConfigProvider>
//         <button className={styles.Reset}>{t("reset")}</button>
//       </div>
//     </div>
//   );
// };
