/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import type { ILogger, ILoggerGroup } from "models";

export const filterLoggers = (loggers: ILogger[], search: string): ILogger[] => {
    const formattedSearch = search.toLowerCase().trim();

    return loggers.filter(({ name }) => name.toLowerCase().includes(formattedSearch));
};

export const filterLoggerGroups = (loggerGroups: ILoggerGroup[], search: string): ILoggerGroup[] => {
    const formattedSearch = search.toLowerCase().trim();

    return loggerGroups.reduce<ILoggerGroup[]>((result, loggerGroup) => {
        const { name, members } = loggerGroup;

        const loggerGroupNameLower = name.toLowerCase();

        if (loggerGroupNameLower.includes(formattedSearch)) {
            result.push(loggerGroup);
            return result;
        }

        const anyLoggerMatches = members.some((member) => member.toLowerCase().includes(formattedSearch));

        if (anyLoggerMatches) {
            result.push(loggerGroup);
            return result;
        }
        return result;
    }, []);
};
