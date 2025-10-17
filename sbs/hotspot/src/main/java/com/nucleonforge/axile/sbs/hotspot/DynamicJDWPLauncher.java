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
