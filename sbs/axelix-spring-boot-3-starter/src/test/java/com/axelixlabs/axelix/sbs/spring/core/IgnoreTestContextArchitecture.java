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
package com.axelixlabs.axelix.sbs.spring.core;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotation must be applied to the test class
 * to indicate that the arch unit check should ignore this test class.
 * <p>
 * When this annotation is applied, the {@link TestContextArchitectureTest} begins to ignore this test class.
 *
 * @see TestContextArchitectureTest
 *
 * @author Artemiy Degtyarev
 * @author Mikhail Polivakha
 */
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface IgnoreTestContextArchitecture {

    String POTENTIAL_CONTEXT_MUTATION = "This test potentially mutates the state of the application context, "
            + "and thus it is unsafe to use the shared context here, since in this case we will have shared mutable "
            + "state which is extremely error prone";

    String NO_SIBLINGS = "This test just does not have (yet!) any siblings to share the TestContext with. We "
            + "typically try to share test contexts within a single exposed capability (like single exposed "
            + "ActuatorEndpoint). This is the only test within this 'capability family' that requires spring's ApplicationContext"
            + " to run, so, there is nothing to share";

    /**
     * Reason why this test must be ignored.
     */
    String reason();
}
