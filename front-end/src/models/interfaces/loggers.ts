import type { ICommonSliceState } from "./globals";

export interface ILogger {
  /**
   * Single logger name
   */
  name: string;
  /**
   * Single logger configured level
   */
  configuredLevel: string;
  /**
   * Single logger current level
   */
  effectiveLevel: string;
}

export interface ILoggersSliceState extends ICommonSliceState {
  /**
   * Levels for all loggers
   */
  levels: string[];
  /**
   * All loggers
   */
  loggers: ILogger[];
  /**
   * Text for searching loggers
   */
  loggersSearchText: string;
  /**
   * Filtered loggers after searching
   */
  filteredLoggers: ILogger[];
}

// todo use this in future
//   groups: [
//     {
//       "name": "string",
//       "configuredLevel": "string",
//       "members": [
//         "string"
//       ]
//     }
//   ], // todo
