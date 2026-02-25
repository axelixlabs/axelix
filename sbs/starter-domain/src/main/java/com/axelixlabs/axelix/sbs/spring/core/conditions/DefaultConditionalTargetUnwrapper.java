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
package com.axelixlabs.axelix.sbs.spring.core.conditions;

/**
 * Default implementation {@link ConditionalTargetUnwrapper}.
 *
 * @author Sergey Cherkasov
 * @author Nikita Kirillov
 */
public class DefaultConditionalTargetUnwrapper implements ConditionalTargetUnwrapper {

    // See SpringBootCondition.getClassOrMethodName method in order to understand why we're doing this.
    // https://github.com/spring-projects/spring-boot/blob/main/core/spring-boot-autoconfigure/src/main/java/org/springframework/boot/autoconfigure/condition/SpringBootCondition.java#L74
    @Override
    public UnwrappedTarget unwrap(String target) {
        if (target.contains("#")) {
            String[] pair = target.split("#");
            return new UnwrappedTarget(pair[0], pair[1]);
        } else {
            return new UnwrappedTarget(target, null);
        }
    }
}
