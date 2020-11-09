package me.halfheart.fortniteautoexporter;


import com.google.gson.*;
import me.fungames.jfortniteparse.fileprovider.DefaultFileProvider;
import me.fungames.jfortniteparse.ue4.versions.Ue4Version;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class main {
    private static final Logger LOGGER = LoggerFactory.getLogger("FortniteAutoExporter");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Config config;
    private static DefaultFileProvider fileProvider;
    private static long start = System.currentTimeMillis();

    public static void main(String[] Args) throws Exception {

        File configFile = new File("config.json");

        if (!configFile.exists()) {
            LOGGER.error("Configuration File not Found");
            LOGGER.info("Creating Configuration File");
            configFile.createNewFile();
            try {
                LOGGER.info("Writing to Configuration File");
                Files.write(Paths.get("config.json"), writeToFile.getBytes());
            } catch (Exception e) {e.printStackTrace();}
        }

        LOGGER.info("Reading Configuration File " + configFile.getAbsolutePath());

        try (FileReader reader = new FileReader(configFile)) {
            config = GSON.fromJson(reader, Config.class)
        }

        File pakDir = new File(config.PakDirectory)

        if (!pakDir.exists()) {
            throw new MainException("Directory " + pakDir.getAbsolutePath() + " doesn't exist.");
        }

        LOGGER.info("Valid Game Files at " + pakDir.getAbsolutePath());

        if (config.UEVersion == null) {
             throw new MainException("Invalid UE Version. Available Versions: " + Arrays.toString(Ue4Version.values()));
        }

        fileProvider = new DefaultFileProvider(pakDir, config.UEVersion);


    }

    public static class Config {
        public static String repo = "https://github.com/24mstrassman/AutoExporter";
        public static String PakDirectory = "D:\\Fortnite 14.30 Backup\\Paks";
        public static Ue4Version UEVersion = Ue4Version.GAME_UE4_LATEST;

    }

    private static class MainException extends Exception {
        public MainException(String message) {
            super(message);
        }
    }
}
