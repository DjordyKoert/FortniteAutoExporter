package me.halfheart.fortniteautoexporter;


import com.google.gson.*;

import me.fungames.jfortniteparse.fileprovider.DefaultFileProvider;
import me.fungames.jfortniteparse.ue4.assets.Package;
import me.fungames.jfortniteparse.ue4.locres.FnLanguage;
import me.fungames.jfortniteparse.ue4.locres.Locres;
import me.fungames.jfortniteparse.ue4.objects.core.misc.FGuid;
import me.fungames.jfortniteparse.ue4.versions.Ue4Version;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;

import java.io.FileWriter;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Scanner;

public class main {
    private static final Logger LOGGER = LoggerFactory.getLogger("FortniteAutoExporter");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Config config;
    private static DefaultFileProvider fileProvider;
    private static String localDir = System.getProperty("user.dir");
    private static long start = System.currentTimeMillis();

    public static void main(String[] Args) throws Exception {
        try {
            File configFile = new File("config.json");

            if (!configFile.exists()) {
                LOGGER.error("Configuration File not Found");
                return;
            }

            LOGGER.info("Reading Configuration File " + configFile.getAbsolutePath());

            try (FileReader reader = new FileReader(configFile)) {
                config = GSON.fromJson(reader, Config.class);
            }

            File pakDir = new File(config.PakDirectory);

            if (!pakDir.exists()) {
                throw new MainException("Directory " + pakDir.getAbsolutePath() + " doesn't exist.");
            }

            LOGGER.info("Valid Game Files at " + pakDir.getAbsolutePath());

            if (config.UEVersion == null) {
                throw new MainException("Invalid UE Version. Available Versions: " + Arrays.toString(Ue4Version.values()));
            }

            String SkinSelection = promptUser("Enter Skin Selection:");

            fileProvider = new DefaultFileProvider(pakDir, config.UEVersion);
            fileProvider.submitKey(FGuid.Companion.getMainGuid(), config.EncryptionKey);
            Locres locres = fileProvider.loadLocres(FnLanguage.EN);

            String formattedCID = String.format("https://benbotfn.tk/api/v1/cosmetics/br/search?name=%s", SkinSelection.replace(" ", "%20"));
            Reader reader = new OkHttpClient().newCall(new Request.Builder().url(formattedCID).build()).execute().body().charStream();
            CharacterReponse cosmeticResponse = GSON.fromJson(reader, CharacterReponse.class);
            reader.close();

            Package pkg = fileProvider.loadGameFile(cosmeticResponse.path + ".uasset");

            if (cosmeticResponse.path == null) {
                throw new MainException("Invalid Skin Selection.");
            }

            if (pkg == null) {
                throw new MainException("Error Parsing Package.");
            }

            checkForLocalDirectory("\\Dumps\\");
            createDirectory(String.format("\\Dumps\\%s\\", cosmeticResponse.name));
            String toJson = pkg.toJson(locres);
            createFile(String.format("\\Dumps\\%s\\", cosmeticResponse.name), cosmeticResponse.id + ".json", toJson);


        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }


    public static String promptUser(String displayText) {
        Scanner sc = new Scanner(System.in);
        System.out.println(displayText);
        String waitForInput = sc.nextLine();
        return waitForInput;
    }

    static void checkForLocalDirectory(String pathInput) throws MainException {
        File dumpDirectory = new File(localDir +  pathInput);
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

    public static void umodelExport() throws Exception {
        ProcessBuilder pb = new ProcessBuilder(Arrays.asList("umodel", "@umodelqueue.txt"));
		pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
		pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        int exitCode = pb.start().waitFor();
        LOGGER.info("Starting UModel Process");
    }

    public static class CharacterReponse {
        public String id;
        public String path;
        public String name;
    }

    public static class Config {
        public static String repo = "https://github.com/24mstrassman/AutoExporter";
        public static String PakDirectory = "D:\\Fortnite 14.30 Backup\\Paks";
        public static Ue4Version UEVersion = Ue4Version.GAME_UE4_LATEST;
        public static String EncryptionKey = "0x3440AB1D1B824905842BE1574F149F9FC7DBA2BB566993E597402B4715A28BD5";
    }

    private static class MainException extends Exception {
        public MainException(String message) {
            super(message);
        }
    }
}
