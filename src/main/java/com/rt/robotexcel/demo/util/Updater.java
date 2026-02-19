package com.rt.robotexcel.demo.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class Updater {

    public static void downloadAndInstall(String downloadUrl) {
        try {
            URL location = Updater.class.getProtectionDomain().getCodeSource().getLocation();
            String path = location.getPath();
            path = URLDecoder.decode(path, StandardCharsets.UTF_8);
            // Path fixes to deal with springboot jar structure
            if (path.startsWith("jar:")) {
                path = path.substring(4);
            }
            if (path.startsWith("file:")) {
                path = path.substring(5);
            }
            if (path.startsWith("nested:")) {
                path = path.substring(7);
            }
            if (path.contains("!")) {
                path = path.substring(0, path.indexOf("!"));
            }
            File actualJarFile = new File(path);
            if (actualJarFile.isDirectory()) {
                System.out.println("Running from IDE/Classes. Update skipped.");
                return;
            }

            File parentDir = actualJarFile.getParentFile();
            File updateJar = new File(parentDir, "update.jar");

            System.out.println("JAR Path: " + actualJarFile.getAbsolutePath());
            System.out.println("Target Update Path: " + updateJar.getAbsolutePath());
            System.out.println("Baixando atualização...");
            try (BufferedInputStream in = new BufferedInputStream(new URL(downloadUrl).openStream());
                    FileOutputStream fileOutputStream = new FileOutputStream(updateJar)) {
                byte dataBuffer[] = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                    fileOutputStream.write(dataBuffer, 0, bytesRead);
                }
            }
            createRestartScriptAndExit(actualJarFile, updateJar);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createRestartScriptAndExit(File currentJar, File updateJar) throws IOException {
        String os = System.getProperty("os.name").toLowerCase();
        File script;
        // If on Windows, create a batch file to update
        if (os.contains("win")) {
            File batchFile = new File(currentJar.getParent(), "update.bat");
            script = new File(currentJar.getParent(), "update.vbs");
            String batContent = "@echo off\r\n" +
                    "timeout /t 2 /nobreak > nul\r\n" +
                    "move /y \"" + updateJar.getAbsolutePath() + "\" \"" + currentJar.getAbsolutePath() + "\"\r\n" +
                    "start \"\" javaw -jar \"" + currentJar.getAbsolutePath() + "\"\r\n" +

                    "del \"" + batchFile.getAbsolutePath() + "\"\r\n" +
                    "del \"" + script.getAbsolutePath() + "\"";

            Files.writeString(batchFile.toPath(), batContent);
            String vbsContent = "CreateObject(\"Wscript.Shell\").Run \"\"\"" + batchFile.getAbsolutePath()
                    + "\"\"\", 0, False";
            Files.writeString(script.toPath(), vbsContent);
            Runtime.getRuntime().exec("wscript \"" + script.getAbsolutePath() + "\"");
        } else {
            // If on Unix/Linux/Mac, create a shell script
            script = new File(currentJar.getParent(), "update.sh");
            String shContent = "#!/bin/bash\n" +
                    "sleep 2\n" +
                    "mv -f \"" + updateJar.getAbsolutePath() + "\" \"" + currentJar.getAbsolutePath() + "\"\n" +
                    "nohup java -jar \"" + currentJar.getAbsolutePath() + "\" > /dev/null 2>&1 &\n" +
                    "rm -- \"$0\"";
            Files.writeString(script.toPath(), shContent);
            script.setExecutable(true);
            Runtime.getRuntime().exec(new String[] { "/bin/bash", script.getAbsolutePath() });

        }

        System.exit(0);
    }
}
