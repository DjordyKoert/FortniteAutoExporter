package me.halfheart.fortniteautoexporter;

import com.google.gson.*;

import static me.halfheart.fortniteautoexporter.basicTools.checkForLocalDirectory;
import static me.halfheart.fortniteautoexporter.basicTools.createDirectory;
import static me.halfheart.fortniteautoexporter.basicTools.createFile;
import static me.halfheart.fortniteautoexporter.basicTools.promptUser;

import static me.halfheart.fortniteautoexporter.ItemDefinitionConversions.CIDtoHIDName;
import static me.halfheart.fortniteautoexporter.ItemDefinitionConversions.CIDtoHIDPath;
import static me.halfheart.fortniteautoexporter.ItemDefinitionConversions.HIDtoHS;

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

import java.io.Reader;
import java.util.Arrays;
import java.util.List;

public class main {
    private static final Logger LOGGER = LoggerFactory.getLogger("FortniteAutoExporter");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Config config;
    private static DefaultFileProvider fileProvider;
    private static CharacterResponse cosmeticResponse;
    private static CharacterPartsPath characterPartsPath;

    private static Package pkg;
    private static Locres locres;

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

            LOGGER.info("Unreal Version: " + config.UEVersion);

            File pakDir = new File(config.PakDirectory);

            if (!pakDir.exists()) {
                throw new MainException("Directory " + pakDir.getAbsolutePath() + " doesn't exist.");
            }

            LOGGER.info("Game Directory: " + pakDir.getAbsolutePath());

            if (config.UEVersion == null) {
                throw new MainException("Invalid UE Version. Available Versions: " + Arrays.toString(Ue4Version.values()));
            }

            String SkinSelection = promptUser("Enter Skin Selection:");

            fileProvider = new DefaultFileProvider(pakDir, config.UEVersion);
            fileProvider.submitKey(FGuid.Companion.getMainGuid(), config.EncryptionKey);
            locres = fileProvider.loadLocres(FnLanguage.EN);

            String formattedCID = String.format("https://benbotfn.tk/api/v1/cosmetics/br/search?name=%s", SkinSelection.replace(" ", "%20"));
            Reader reader = new OkHttpClient().newCall(new Request.Builder().url(formattedCID).build()).execute().body().charStream();
            cosmeticResponse = GSON.fromJson(reader, CharacterResponse.class);
            reader.close();

            if (config.dumpAssets) {
                checkForLocalDirectory("\\Dumps\\");
                createDirectory(String.format("\\Dumps\\%s\\", cosmeticResponse.name));
            }

            pkg = fileProvider.loadGameFile(cosmeticResponse.path + ".uasset");

            if (cosmeticResponse.path == null) {
                throw new MainException("Invalid Skin Selection.");
            }

            if (pkg == null) {
                throw new MainException("Error Parsing Package.");
            }

            skinToParts();

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    public static void umodelExport() throws Exception {
        ProcessBuilder pb = new ProcessBuilder(Arrays.asList("umodel", "@umodelqueue.txt"));
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        int exitCode = pb.start().waitFor();
        LOGGER.info("Starting UModel Process");
    }

    public static void skinToParts() throws Exception {

        String toJson = pkg.toJson(locres); // CID Parse

        if (config.dumpAssets) {
            createFile(String.format("\\Dumps\\%s\\", cosmeticResponse.name), cosmeticResponse.id + ".json", toJson);
        }

        String CIDtoHIDPath = CIDtoHIDPath(toJson);
        String CIDtoHIDName = CIDtoHIDName(toJson);

        pkg = fileProvider.loadGameFile(CIDtoHIDPath + ".uasset");
        toJson = pkg.toJson(locres); // HID Parse
        if (config.dumpAssets) {
            createFile(String.format("\\Dumps\\%s\\", cosmeticResponse.name), CIDtoHIDName + ".json", toJson);
        }

        String HIDtoHSPath = HIDtoHS(toJson, 0);
        String HIDtoHSName = HIDtoHS(toJson, 1);

        pkg = fileProvider.loadGameFile(HIDtoHSPath + ".uasset");
        toJson = pkg.toJson(locres); // HS Parse
        if (config.dumpAssets) {
            createFile(String.format("\\Dumps\\%s\\", cosmeticResponse.name), HIDtoHSName + ".json", toJson);
        }

        String assetPathFormat;
        String assetNameFormat;
        JsonObject baseJSON = GSON.fromJson(toJson, JsonObject.class);
        JsonArray exportPropertiesArray = baseJSON.getAsJsonArray("export_properties");
        for (JsonElement temp1 : exportPropertiesArray) {
            JsonObject exportPropertiesObject = temp1.getAsJsonObject();
            JsonArray characterPartsArray = exportPropertiesObject.getAsJsonArray("CharacterParts");
            int i = 0;
            for (JsonElement temp2 : characterPartsArray) {
                i++;
                JsonObject characterPartsObject = temp2.getAsJsonObject();
                String assetUnformat = characterPartsObject.get("assetPath").getAsString();
                String[] splitparts = assetUnformat.split("\\.");
                if (i == 1) {
                    assetPathFormat = splitparts[0];
                    assetNameFormat = splitparts[1];

                    characterPartsPath.CPPath1 = assetPathFormat;
                    pkg = fileProvider.loadGameFile(assetPathFormat + ".uasset");
                    toJson = pkg.toJson(locres); // HS Parse
                    if (config.dumpAssets) {
                        createFile(String.format("\\Dumps\\%s\\", cosmeticResponse.name), assetNameFormat + ".json", toJson);
                    }
                } else if (i == 2) {
                    assetPathFormat = splitparts[0];
                    assetNameFormat = splitparts[1];

                    characterPartsPath.CPPath2 = assetPathFormat;
                    pkg = fileProvider.loadGameFile(assetPathFormat + ".uasset");
                    toJson = pkg.toJson(locres); // HS Parse
                    if (config.dumpAssets) {
                        createFile(String.format("\\Dumps\\%s\\", cosmeticResponse.name), assetNameFormat + ".json", toJson);
                    }
                } else if (i == 3) {
                    assetPathFormat = splitparts[0];
                    assetNameFormat = splitparts[1];

                    characterPartsPath.CPPath3 = assetPathFormat;
                    pkg = fileProvider.loadGameFile(assetPathFormat + ".uasset");
                    toJson = pkg.toJson(locres); // HS Parse
                    if (config.dumpAssets) {
                        createFile(String.format("\\Dumps\\%s\\", cosmeticResponse.name), assetNameFormat + ".json", toJson);
                    }
                } else if (i == 4) {
                    assetPathFormat = splitparts[0];
                    assetNameFormat = splitparts[1];

                    characterPartsPath.CPPath4 = assetPathFormat;
                    pkg = fileProvider.loadGameFile(assetPathFormat + ".uasset");
                    toJson = pkg.toJson(locres); // HS Parse
                    if (config.dumpAssets) {
                        createFile(String.format("\\Dumps\\%s\\", cosmeticResponse.name), assetNameFormat + ".json", toJson);
                    }
                } else if (i == 5) {
                    assetPathFormat = splitparts[0];
                    assetNameFormat = splitparts[1];

                    characterPartsPath.CPPath5 = assetPathFormat;
                    pkg = fileProvider.loadGameFile(assetPathFormat + ".uasset");
                    toJson = pkg.toJson(locres); // HS Parse
                    if (config.dumpAssets) {
                        createFile(String.format("\\Dumps\\%s\\", cosmeticResponse.name), assetNameFormat + ".json", toJson);
                    }
                }
            }
        }


    }

    public static class CharacterPartsPath {
        public static String CPPath1;
        public static String CPPath2;
        public static String CPPath3;
        public static String CPPath4;
        public static String CPPath5;

    }
    public static class CombinedMeshes {
        public static String charPart1;
        public static String charPart2;
        public static String charPart3;
        public static String charPart4;
        public static String charPart5;
    }

    public static class CharacterResponse {
        public String id;
        public String path;
        public String name;
    }
    public static class Config {
        public String repo = "https://github.com/24mstrassman/AutoExporter";
        public String PakDirectory = "D:\\Fortnite 14.30 Backup\\Paks";
        public Ue4Version UEVersion = Ue4Version.GAME_UE4_LATEST;
        public String EncryptionKey = "0x3440AB1D1B824905842BE1574F149F9FC7DBA2BB566993E597402B4715A28BD5";
        public boolean dumpAssets = false;
    }
    private static class MainException extends Exception {
        public MainException(String message) {
            super(message);
        }
    }
}

