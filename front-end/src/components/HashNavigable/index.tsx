/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
import accordionStyles from "components/Accordion/styles.module.css";
import { type PropsWithChildren, useEffect } from "react";
import { useLocation } from "react-router-dom";

import styles from "./styles.module.css";

interface IProps {
    /**
     * The class to which we add a highlight style to indicate that it has been scrolled into view
     */
    className?: string;
}

export const HashNavigable = ({ children, className = accordionStyles.HeaderWrapper }: PropsWithChildren<IProps>) => {
    const { hash } = useLocation();

    // TODO:
    //  We have to use useEffect hook here since the page is rendered by the browser initially
    //  when there is no data yet backing the configprops table. Therefore, there is no element
    //  with the requested 'hash', and thus the browser simply cannot navigate to the element that
    //  is just not yet loaded from the backend. Once the data is loaded, the browser will not re-attempt
    //  to re-navigate to the requested 'hash', and therefore we have to do it manually here.
    useEffect(() => {
        if (!hash) {
            return;
        }

        const element = document.querySelector(hash);
        if (!element) {
            return;
        }

        element.scrollIntoView();

        const header = element.querySelector(`.${className}`);
        if (!header) {
            return;
        }

        header.classList.add(styles.Highlight);
    }, [hash]);

    return <>{children}</>;
};
