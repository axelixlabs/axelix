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
import { type ComponentType, type FC, type PropsWithChildren, Suspense } from "react";

import { LinearProgress } from "../LinearProgress";

function Loadable<P extends object>(Component: ComponentType<P>): FC<P> {
    return (props: PropsWithChildren<P>) => (
        <Suspense fallback={<LinearProgress />}>
            <Component {...props} />
        </Suspense>
    );
}

export default Loadable;
