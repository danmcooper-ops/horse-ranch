package com.ranchgame.lwjgl3;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;

/**
 * On macOS, GLFW must run on the first thread of the process, which needs the
 * -XstartOnFirstThread JVM flag. When launched without it, this helper
 * relaunches the JVM with the flag added (same trick gdx-liftoff projects use).
 */
public final class StartupHelper {

    private static final String RESTARTED_FLAG = "horseranch.restarted";

    private StartupHelper() {
    }

    public static boolean startNewJvmIfRequired() {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (!os.contains("mac")) return false;
        if ("1".equals(System.getProperty(RESTARTED_FLAG))) return false;
        // already launched correctly?
        for (String arg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
            if ("-XstartOnFirstThread".equals(arg)) return false;
        }
        String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        ArrayList<String> command = new ArrayList<>();
        command.add(javaBin);
        command.add("-XstartOnFirstThread");
        command.add("-D" + RESTARTED_FLAG + "=1");
        command.add("-cp");
        command.add(System.getProperty("java.class.path"));
        command.add(Lwjgl3Launcher.class.getName());
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            process.waitFor();
        } catch (Exception e) {
            System.err.println("Failed to relaunch JVM with -XstartOnFirstThread: " + e);
            return false;
        }
        return true;
    }
}
