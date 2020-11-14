/** Fortnite Auto Exporter
 * Created by Half
 **/
package com.halfheart.fortniteautoexporter;

import com.google.gson.*;

import static com.halfheart.fortniteautoexporter.basicTools.checkForLocalDirectory;
import static com.halfheart.fortniteautoexporter.basicTools.createDirectory;
import static com.halfheart.fortniteautoexporter.basicTools.createFile;
import static com.halfheart.fortniteautoexporter.basicTools.promptUser;

import static com.halfheart.fortniteautoexporter.ItemDefinitionConversions.CIDtoHIDName;
import static com.halfheart.fortniteautoexporter.ItemDefinitionConversions.CIDtoHIDPath;
import static com.halfheart.fortniteautoexporter.ItemDefinitionConversions.HIDtoHS;

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

import java.io.*;

import java.util.Arrays;
public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger("FortniteAutoExporter");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String localDir = System.getProperty("user.dir");

    private static Config config;
    private static DefaultFileProvider fileProvider;
    private static CharacterResponse cosmeticResponse;

    private static Package pkg;
    private static Locres locres;

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
            umodelExport();

            JsonObject root = new JsonObject();
            try {
                root.addProperty("assetPath1", localDir + "\\UmodelExport" + CombinedMeshes.charPart1.replace("/", "\\")+ ".psk");
                root.addProperty("assetPath2", localDir + "\\UmodelExport" + CombinedMeshes.charPart2.replace("/", "\\") + ".psk");
                root.addProperty("assetPath3", localDir + "\\UmodelExport" + CombinedMeshes.charPart3.replace("/", "\\") + ".psk");
                root.addProperty("assetPath4", localDir + "\\UmodelExport" + CombinedMeshes.charPart4.replace("/", "\\") + ".psk");
                root.addProperty("assetPath5", localDir + "\\UmodelExport" + CombinedMeshes.charPart5.replace("/", "\\") + ".psk");
            } catch (Throwable e) {}
            File processedFile = new File("processed.json");
            processedFile.createNewFile();
            FileWriter writer = new FileWriter(processedFile);
            writer.write(GSON.toJson(root));
            writer.close();
        } catch (Exception e) {

            e.printStackTrace();
        }
        System.out.println("\nReplace workingDirectory in the python script with: \n\"" + localDir + "\"\n");
        LOGGER.info("Finished Exporting.");
        System.exit(0);
    }

    public static void umodelExport() throws Exception {
        try (PrintWriter printWriter = new PrintWriter("umodel_queue.txt")) {
            printWriter.println("-path=\"" + config.PakDirectory + "\"");
            String[] SplitUEVersion = config.UEVersion.toString().split("_");
            printWriter.println("-game=ue4." + SplitUEVersion[2]);
            printWriter.println("-aes=" + config.EncryptionKey);
            printWriter.println("-export ");
            printWriter.println("-pkg=" + CombinedMeshes.charPart1);
            printWriter.println("-pkg=" + CombinedMeshes.charPart2);
            printWriter.println("-pkg=" + CombinedMeshes.charPart3);
            printWriter.println("-pkg=" + CombinedMeshes.charPart4);
            printWriter.println("-pkg=" + CombinedMeshes.charPart5);

        }
        ProcessBuilder pb = new ProcessBuilder(Arrays.asList("umodel", "@umodel_queue.txt"));
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        int exitCode = pb.start().waitFor();
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

        HStoCP HStoCP = GSON.fromJson(toJson, HStoCP.class);
        for (int i = 0; i < 5; i++) {
            try {
                String[] splitparts = HStoCP.export_properties[0].CharacterParts[i].assetPath.split("\\.");
                String MeshPath = splitparts[0];
                String MeshName = splitparts[1];
                if (i == 0) {
                    CharacterParts.CPPath1 = MeshPath;
                    CharacterParts.CPName1 = MeshName;

                    pkg = fileProvider.loadGameFile(CharacterParts.CPPath1 + ".uasset");
                    toJson = pkg.toJson(locres);
                    if (config.dumpAssets) {
                        createFile(String.format("\\Dumps\\%s\\", cosmeticResponse.name), CharacterParts.CPName1 + ".json", toJson);
                    }
                    CPtoMesh cptoMesh = GSON.fromJson(toJson, CPtoMesh.class);
                    String[] MeshSplit = cptoMesh.export_properties[1].SkeletalMesh.assetPath.split("\\.");
                    CombinedMeshes.charPart1 = MeshSplit[0];
                } else if (i == 1) {
                    CharacterParts.CPPath2 = MeshPath;
                    CharacterParts.CPName2 = MeshName;

                    pkg = fileProvider.loadGameFile(CharacterParts.CPPath2 + ".uasset");
                    toJson = pkg.toJson(locres);
                    if (config.dumpAssets) {
                        createFile(String.format("\\Dumps\\%s\\", cosmeticResponse.name), CharacterParts.CPName2 + ".json", toJson);
                    }

                    CPtoMesh cptoMesh = GSON.fromJson(toJson, CPtoMesh.class);
                    String[] MeshSplit = cptoMesh.export_properties[1].SkeletalMesh.assetPath.split("\\.");
                    CombinedMeshes.charPart2 = MeshSplit[0];

                } else if (i == 2) {
                    CharacterParts.CPPath3 = MeshPath;
                    CharacterParts.CPName3 = MeshName;

                    pkg = fileProvider.loadGameFile(CharacterParts.CPPath3 + ".uasset");
                    toJson = pkg.toJson(locres);
                    if (config.dumpAssets) {
                        createFile(String.format("\\Dumps\\%s\\", cosmeticResponse.name), CharacterParts.CPName3 + ".json", toJson);
                    }

                    CPtoMesh cptoMesh = GSON.fromJson(toJson, CPtoMesh.class);
                    String[] MeshSplit = cptoMesh.export_properties[1].SkeletalMesh.assetPath.split("\\.");
                    CombinedMeshes.charPart3 = MeshSplit[0];
                } else if (i == 3) {
                    CharacterParts.CPPath4 = MeshPath;
                    CharacterParts.CPName4 = MeshName;

                    pkg = fileProvider.loadGameFile(CharacterParts.CPPath4 + ".uasset");
                    toJson = pkg.toJson(locres);
                    if (config.dumpAssets) {
                        createFile(String.format("\\Dumps\\%s\\", cosmeticResponse.name), CharacterParts.CPName4 + ".json", toJson);
                    }

                    CPtoMesh cptoMesh = GSON.fromJson(toJson, CPtoMesh.class);
                    String[] MeshSplit = cptoMesh.export_properties[1].SkeletalMesh.assetPath.split("\\.");
                    CombinedMeshes.charPart4 = MeshSplit[0];
                } else if (i == 4) {
                    CharacterParts.CPPath5 = MeshPath;
                    CharacterParts.CPName5 = MeshName;

                    pkg = fileProvider.loadGameFile(CharacterParts.CPPath5 + ".uasset");
                    toJson = pkg.toJson(locres);
                    if (config.dumpAssets) {
                        createFile(String.format("\\Dumps\\%s\\", cosmeticResponse.name), CharacterParts.CPName5 + ".json", toJson);
                    }
                    CPtoMesh cptoMesh = GSON.fromJson(toJson, CPtoMesh.class);
                    String[] MeshSplit = cptoMesh.export_properties[1].SkeletalMesh.assetPath.split("\\.");
                    CombinedMeshes.charPart5 = MeshSplit[0];
                }
            } catch (Exception e) {
                continue;
            }
        }

    }

    public static class CharacterParts {
        public static String CPPath1;
        public static String CPPath2;
        public static String CPPath3;
        public static String CPPath4;
        public static String CPPath5;
        public static String CPName1;
        public static String CPName2;
        public static String CPName3;
        public static String CPName4;
        public static String CPName5;

    }

    public static class CombinedMeshes {
        public static String charPart1;
        public static String charPart2;
        public static String charPart3;
        public static String charPart4;
        public static String charPart5;
    }

    public static class HStoCP {
            private CharacterParts[] export_properties;

            public class CharacterParts {
                public AssetPath[] CharacterParts;
            }

            public class AssetPath {
                public String assetPath;
            }

        }

    public static class CPtoMesh {
            private SkeletalMesh[] export_properties;

            public class SkeletalMesh {
                public AssetPath SkeletalMesh;
            }

            public class AssetPath {
                public String assetPath;
            }
        }

    public static class CharacterResponse {
            public String id;
            public String path;
            public String name;
        }

    public static class Config {
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

