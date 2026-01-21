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
import { Accordion } from "components";
import type { ITransactionalResponseData } from "models";

import { TransactionalAccordionBody } from "./TransactionalAccordionBody";
import { TransactionalAccordionHeader } from "./TransactionalAccordionHeader";

export interface IProps {
    /**
     * Effective transactional data
     */
    effectiveTransactionalData: ITransactionalResponseData[];
}

export const TransactionalList = ({ effectiveTransactionalData }: IProps) => {
    return (
        <div className="AccordionsWrapper">
            {effectiveTransactionalData.map((transactional) => (
                <Accordion
                    header={<TransactionalAccordionHeader transactional={transactional} />}
                    children={<TransactionalAccordionBody transactional={transactional} />}
                    key={transactional.className + transactional.methodName}
                />
            ))}
        </div>
    );
};
