import type { ICommonSliceState } from "./globals";

export interface ILogger {
  /**
   * Logger name
   */
  name: string;
  /**
   * Explicitly configured level for logger, if any 
   */
  configuredLevel?: string;
  /**
   * Single logger current level
   */
  effectiveLevel: string;
}

export interface ILoggerData {
  /**
     * Levels for all loggers
     */
  levels: string[];
  /**
   * All loggers
   */
  loggers: ILogger[];
}

export interface ILoggersSliceState extends ICommonSliceState, ILoggerData {
  /**
   * Text for searching loggers
   */
  loggersSearchText: string;
  /**
   * Filtered loggers after searching
   */
  filteredLoggers: ILogger[];
  updateLoggerSuccess: boolean
}

export interface ISetLoggerLevelRequestData {
  /**
   * Service id
   */
  id: string;
  /**
   * Logger name
   */
  loggerName: string;
  /**
   * Selected level
   */
  loggingLevel: string
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
