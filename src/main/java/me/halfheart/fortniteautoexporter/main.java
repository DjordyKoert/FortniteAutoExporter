package me.halfheart.fortniteautoexporter;


import me.fungames.jfortniteparse.fileprovider.FileProvider;
import me.fungames.jfortniteparse.ue4.versions.Ue4Version;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;


public class main {
    private static final Logger LOGGER = LoggerFactory.getLogger("FortniteAutoExporter");
    private static Config config;
    private static FileProvider fileProvider;

    public static void main(String[] Args) throws Exception {

        File configFile = new File("config.json");

        if (!configFile.exists()) {
            LOGGER.error("Configuration File not Found.");
            LOGGER.info("Creating Configuration File");
            configFile.createNewFile();
            try {
                JSONObject rootConfigJSON = new JSONObject();
                rootConfigJSON.put("Repo", config.repo);
                rootConfigJSON.put("PaksDirectory", config.PaksDirectory);
                rootConfigJSON.put("UEVersion", config.UEVersion);
                String writeToFile = rootConfigJSON.toString(4);
                LOGGER.info("Writing to Configuration File");
                Files.write(Paths.get("config.json"), writeToFile.getBytes());
            } catch (Exception e) {e.printStackTrace();}
        }

        LOGGER.info("Reading Configuration File " + configFile.getAbsolutePath());

    }

    public static class Config {
        public static String repo = "https://github.com/24mstrassman/AutoExporter";
        public static String PaksDirectory = "D:\\Fortnite 14.30 Backup\\Paks";
        public static Ue4Version UEVersion = Ue4Version.GAME_UE4_LATEST;

    }
}
