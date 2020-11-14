package com.halfheart.fortniteautoexporter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class basicTools {
    private static final Logger LOGGER = LoggerFactory.getLogger("FortniteAutoExporter");
    private static String localDir = System.getProperty("user.dir");

    public static String promptUser(String displayText) {
        Scanner sc = new Scanner(System.in);
        System.out.println(displayText);
        String waitForInput = sc.nextLine();
        return waitForInput;
    }

    static void checkForLocalDirectory(String pathInput) throws MainException {
        File dumpDirectory = new File(localDir + pathInput);
        if (!dumpDirectory.exists()) {
            LOGGER.error("Directory " + dumpDirectory + " doesn't exist.");
            try {
                Path path = Paths.get(dumpDirectory.toString());
                Files.createDirectories(path);
            } catch (Exception e) {
                throw new MainException("Failed to Create Directory " + pathInput);
            }
            LOGGER.info("Created Directory " + dumpDirectory);
        }
    }

    static void createDirectory(String pathInput) throws Exception {
        Path path = Paths.get(localDir + pathInput);
        Files.createDirectories(path);
    }

    static void createFile(String Path, String Name, String Content) throws Exception {
        FileWriter myWriter = new FileWriter(localDir + Path + Name);
        myWriter.write(Content);
        myWriter.close();
    }

    private static class MainException extends Exception {
        public MainException(String message) {
            super(message);
        }
    }
}
