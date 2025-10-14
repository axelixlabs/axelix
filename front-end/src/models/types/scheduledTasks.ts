import type { getScheduledTasksTypes } from "utils";

/**
 * Represents the union of all keys returned by `getScheduledTasksTypes`.
 * This type is used to ensure type safety when referencing specific scheduled task identifiers.
 */
export type TaskKey = keyof ReturnType<typeof getScheduledTasksTypes>;
