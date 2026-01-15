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
import LinkIcon from "assets/icons/link.svg?react";
import type { PropsWithChildren } from "react";
import { Link } from "react-router-dom";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Link href
     */
    href: string;
}

export const StyledLink = ({ children, href }: PropsWithChildren<IProps>) => {
    return (
        <Link to={href} className={styles.MainWrapper}>
            {children}
            <LinkIcon className={styles.LinkIcon} />
        </Link>
    );
};
