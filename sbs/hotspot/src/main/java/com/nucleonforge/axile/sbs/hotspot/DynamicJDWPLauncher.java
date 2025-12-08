/*
 * Copyright 2025-present the original author or authors.
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
package com.nucleonforge.axile.sbs.hotspot;

import java.io.IOException;

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

public class DynamicJDWPLauncher {

    /**
     * @param port on which the jdwp debugging window needs to be opened.
     */
    public boolean launchDebugging(int port) {
        long processId = ProcessHandle.current().pid();

        try {
            VirtualMachine currentVm = VirtualMachine.attach(String.valueOf(processId));
            //            currentVm.loadAgentLibrary("jdwp", );

            throw new UnsupportedOperationException();

        } catch (AttachNotSupportedException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
