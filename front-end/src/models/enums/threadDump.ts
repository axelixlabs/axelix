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
export enum EThreadState {
    NEW = "NEW",
    RUNNABLE = "RUNNABLE",
    BLOCKED = "BLOCKED",
    WAITING = "WAITING",
    TIMED_WAITING = "TIMED_WAITING",
    TERMINATED = "TERMINATED",
}

export enum EThreadDumpStateColors {
    BLUE = "#1890ff",
    GREEN = "#52c41a",
    ORANGE = "#faad14",
    RED = "#ff4d4f",
    YELLOW = "#fadb14",
    WHITE = "#fff",
    GREY = "#838383",
}
