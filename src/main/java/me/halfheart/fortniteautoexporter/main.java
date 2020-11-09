package me.halfheart.fortniteautoexporter;


import me.fungames.jfortniteparse.ue4.versions.Ue4Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;


public class main {
    private static final Logger LOGGER = LoggerFactory.getLogger("FortniteAutoExporter");
    private static Config config;

    public static void main(String[] Args) throws Exception {

        File configFile = new File("config.json");

        if (!configFile.exists()) {
            LOGGER.error("Configuration File not Found.");
            LOGGER.info("Creating Configuration File");
            configFile.createNewFile();
            try {
                String writeToFile = config.PaksDirectory;
                LOGGER.info("Writing to Configuration File");
                Files.write(Paths.get("config.json"), writeToFile.getBytes());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        LOGGER.info("Reading Configuration File " + configFile.getAbsolutePath());
    }
    public static class Config {
        public static String PaksDirectory = "D:\\Fortnite\\";
        public static Ue4Version UEVersion = Ue4Version.GAME_UE4_LATEST;
    }
}
