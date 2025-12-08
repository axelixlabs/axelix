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
export enum ECopyableField {
    COMMIT_SHA_SHORT = "commitShaShort",
    BRANCH = "branch",
    ARTIFACT = "artifact",
}

/**
 * The particular component of the state that can be exported.
 */
export enum EExportableComponent {
    HEAP_DUMP = "HEAP_DUMP",
    THREAD_DUMP = "THREAD_DUMP",
    BEANS = "BEANS",
    CACHES = "CACHES",
    CONDITIONS = "CONDITIONS",
    CONFIG_PROPS = "CONFIG_PROPS",
    ENV = "ENV",
    LOG_FILE = "LOG_FILE",
    SCHEDULED_TASKS = "SCHEDULED_TASKS",
}
