/** Fortnite Auto Exporter by Half
 **/
package com.halfheart.fortniteautoexporter;

import com.google.gson.*;

import static com.halfheart.fortniteautoexporter.basicTools.checkForLocalDirectory;
import static com.halfheart.fortniteautoexporter.basicTools.createDirectory;
import static com.halfheart.fortniteautoexporter.basicTools.createFile;
import static com.halfheart.fortniteautoexporter.basicTools.promptUser;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger("FortniteAutoExporter");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String localDir = System.getProperty("user.dir");

    private static Config config;
    private static DefaultFileProvider fileProvider;
    private static CharacterResponse[] cosmeticResponse;
    private static AESResponse AESResponse;

    private static Package pkg;
    private static Locres locres;
    
    private static File pakDir;
    private static String VersionSelection;

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
            
            config.EncryptionKey = attemptGetAESKey();
            
            pakDir = new File(config.PaksDirectory);

            if (!pakDir.exists()) {
                throw new CustomException("Directory " + pakDir.getAbsolutePath() + " doesn't exist.");
            }

            LOGGER.info("Game Directory: " + pakDir.getAbsolutePath());

            if (config.UEVersion == null) {
                throw new CustomException("Invalid UE Version. Available Versions: " + Arrays.toString(Ue4Version.values()));
            }

            selectSkinPromt();
        } catch (Exception e) {

            e.printStackTrace();
        }
        System.exit(0);
    }

    public static void umodelExport() throws Exception {
        try (PrintWriter printWriter = new PrintWriter("umodel_queue.txt")) {
            printWriter.println("-path=\"" + config.PaksDirectory + "\"");
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
        pb.start().waitFor();
    }
    public static void selectSkinPromt() throws Exception {
    	String SkinSelection = promptUser("(CTRL+C to quit) Enter Skin Selection:");
        String formattedCID = String.format("https://benbotfn.tk/api/v1/cosmetics/br/search/all?lang=en&searchLang=en&matchMethod=full&name=%s&backendType=AthenaCharacter", SkinSelection.replace(" ", "%20"));
        Reader reader = new OkHttpClient().newCall(new Request.Builder().url(formattedCID).build()).execute().body().charStream();
        cosmeticResponse = GSON.fromJson(reader, CharacterResponse[].class);
        reader.close();

        if (cosmeticResponse.length == 0) {
        	System.err.println("Skin Not Found.");
        	selectSkinPromt();
        }
        if (cosmeticResponse[0].path == null) {
        	System.err.println("Invalid Skin Selection.");
        	selectSkinPromt();
        }

        fileProvider = new DefaultFileProvider(pakDir, config.UEVersion);
        fileProvider.submitKey(FGuid.Companion.getMainGuid(), config.EncryptionKey);
        locres = fileProvider.loadLocres(FnLanguage.EN);

        if (config.dumpAssets) {
            checkForLocalDirectory("\\Dumps\\");
            createDirectory(String.format("\\Dumps\\%s\\", cosmeticResponse[0].name));
        }

        pkg = fileProvider.loadGameFile(cosmeticResponse[0].path + ".uasset");

        if (pkg == null) {
            throw new CustomException("Error Parsing Package.");
        }

        skinToParts();
        umodelExport();

        System.out.println("\nReplace workingDirectory in the python script with: \n\"" + localDir + "\"\n");
        LOGGER.info("Finished Exporting.");
        
        selectSkinPromt();
    }
    
    public static String attemptGetAESKey() throws Exception{
//    	Attempt to find version in PAK Directory name
    	Pattern pattern = Pattern.compile("[0-9][0-9]\\W[0-9][0-9]");
    	Matcher matcher = pattern.matcher(config.PaksDirectory);

    	if (matcher.find()) {
    		LOGGER.info("Detected PAK Version: '" + matcher.group(0) + "', From PAK Directory: '" + config.PaksDirectory + "'");
	    	
	    	VersionSelection = matcher.group(0);
    	}
    	else {    		
    		VersionSelection = promptUser("(CTRL+C to quit) Enter PAK Version:");
    	}
        String VersionSelectionFormatted = String.format("%s",VersionSelection);
        
        String formattedCID = String.format("https://benbotfn.tk/api/v1//aes?version=%s", VersionSelectionFormatted.replace(" ", "."));
        Reader reader = new OkHttpClient().newCall(new Request.Builder().url(formattedCID).build()).execute().body().charStream();
        AESResponse = GSON.fromJson(reader, AESResponse.class);
        reader.close();
        
        if (AESResponse == null) {
        	LOGGER.info("Using AESKey From Config: '" + AESResponse.mainKey +"'");
        }
    	
        LOGGER.info("Found AESKey From API: '" + AESResponse.mainKey + "'");
    	return AESResponse.mainKey;
    }
    
    public static void skinToParts() throws Exception{

        String toJson = pkg.toJson(locres); // CID Parse

        if (config.dumpAssets) {
            createFile(String.format("\\Dumps\\%s\\", cosmeticResponse[0].name), cosmeticResponse[0].id + ".json", toJson);
        }


        CIDtoHID CIDtoHID = GSON.fromJson(toJson, Main.CIDtoHID.class);
        String CIDtoHIDPath = "";
        String CIDtoHIDName = "";
        try {
            for (int i = 0; i < 10; i++) {
                if (CIDtoHID.import_map[i].class_name.equals("FortHeroType")) {
                    CIDtoHIDName = CIDtoHID.import_map[i].object_name;
                } else if (CIDtoHID.import_map[i].class_name.equals("Package") && CIDtoHID.import_map[i].object_name.contains("HID")) {
                    CIDtoHIDPath = CIDtoHID.import_map[i].object_name;
                }
            }
        } catch (Throwable e) {}

        pkg = fileProvider.loadGameFile(CIDtoHIDPath + ".uasset");
        toJson = pkg.toJson(locres); // HID Parse
        if (config.dumpAssets) {
            createFile(String.format("\\Dumps\\%s\\", cosmeticResponse[0].name), CIDtoHIDName + ".json", toJson);
        }

        HIDtoHS HIDtoHS = GSON.fromJson(toJson, HIDtoHS.class);
        String HIDtoHSPath = "";
        String HIDtoHSName = "";
        try {
            for (int i = 0; i < 10; i++) {
                String[] SplitHIDtoHS = HIDtoHS.export_properties[i].Specializations[0].assetPath.split("\\.");
                HIDtoHSPath = SplitHIDtoHS[0];
                HIDtoHSName = SplitHIDtoHS[1];
                }
        } catch (Throwable e) {}

        pkg = fileProvider.loadGameFile(HIDtoHSPath + ".uasset");
        toJson = pkg.toJson(locres); // HS Parse
        if (config.dumpAssets) {
            createFile(String.format("\\Dumps\\%s\\", cosmeticResponse[0].name), HIDtoHSName + ".json", toJson);
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
                        createFile(String.format("\\Dumps\\%s\\", cosmeticResponse[0].name), CharacterParts.CPName1 + ".json", toJson);
                    }
                    CPtoMesh cptoMesh = GSON.fromJson(toJson, CPtoMesh.class);
                    if (toJson.contains("OverrideMaterial")) {
                        System.out.println("Override Found!!");
                        System.out.println(cptoMesh.export_properties[1].MaterialOverrides[0].OverrideMaterial.assetPath);
                    }
                    String[] MeshSplit = cptoMesh.export_properties[1].SkeletalMesh.assetPath.split("\\.");
                    CombinedMeshes.charPart1 = MeshSplit[0];
                } else if (i == 1) {
                    CharacterParts.CPPath2 = MeshPath;
                    CharacterParts.CPName2 = MeshName;

                    pkg = fileProvider.loadGameFile(CharacterParts.CPPath2 + ".uasset");
                    toJson = pkg.toJson(locres);
                    if (config.dumpAssets) {
                        createFile(String.format("\\Dumps\\%s\\", cosmeticResponse[0].name), CharacterParts.CPName2 + ".json", toJson);
                    }
                    CPtoMesh cptoMesh = GSON.fromJson(toJson, CPtoMesh.class);
                    if (toJson.contains("OverrideMaterial")) {
                        System.out.println("Override Found!!");
                        System.out.println(cptoMesh.export_properties[1].MaterialOverrides[0].OverrideMaterial.assetPath);
                    }
                    String[] MeshSplit = cptoMesh.export_properties[1].SkeletalMesh.assetPath.split("\\.");
                    CombinedMeshes.charPart2 = MeshSplit[0];

                } else if (i == 2) {
                    CharacterParts.CPPath3 = MeshPath;
                    CharacterParts.CPName3 = MeshName;

                    pkg = fileProvider.loadGameFile(CharacterParts.CPPath3 + ".uasset");
                    toJson = pkg.toJson(locres);
                    if (config.dumpAssets) {
                        createFile(String.format("\\Dumps\\%s\\", cosmeticResponse[0].name), CharacterParts.CPName3 + ".json", toJson);
                    }
                    CPtoMesh cptoMesh = GSON.fromJson(toJson, CPtoMesh.class);
                    if (toJson.contains("OverrideMaterial")) {
                        System.out.println("Override Found!!");
                        System.out.println(cptoMesh.export_properties[1].MaterialOverrides[0].OverrideMaterial.assetPath);
                    }
                    String[] MeshSplit = cptoMesh.export_properties[1].SkeletalMesh.assetPath.split("\\.");
                    CombinedMeshes.charPart3 = MeshSplit[0];
                } else if (i == 3) {
                    CharacterParts.CPPath4 = MeshPath;
                    CharacterParts.CPName4 = MeshName;

                    pkg = fileProvider.loadGameFile(CharacterParts.CPPath4 + ".uasset");
                    toJson = pkg.toJson(locres);
                    if (config.dumpAssets) {
                        createFile(String.format("\\Dumps\\%s\\", cosmeticResponse[0].name), CharacterParts.CPName4 + ".json", toJson);
                    }
                    CPtoMesh cptoMesh = GSON.fromJson(toJson, CPtoMesh.class);
                    if (toJson.contains("OverrideMaterial")) {
                        System.out.println("Override Found!!");
                        System.out.println(cptoMesh.export_properties[1].MaterialOverrides[0].OverrideMaterial.assetPath);
                    }
                    String[] MeshSplit = cptoMesh.export_properties[1].SkeletalMesh.assetPath.split("\\.");
                    CombinedMeshes.charPart4 = MeshSplit[0];
                } else if (i == 4) {
                    CharacterParts.CPPath5 = MeshPath;
                    CharacterParts.CPName5 = MeshName;

                    pkg = fileProvider.loadGameFile(CharacterParts.CPPath5 + ".uasset");
                    toJson = pkg.toJson(locres);
                    if (config.dumpAssets) {
                        createFile(String.format("\\Dumps\\%s\\", cosmeticResponse[0].name), CharacterParts.CPName5 + ".json", toJson);
                    }
                    CPtoMesh cptoMesh = GSON.fromJson(toJson, CPtoMesh.class);
                    if (toJson.contains("OverrideMaterial")) {
                        System.out.println("Override Found!!");
                        System.out.println(cptoMesh.export_properties[1].MaterialOverrides[0].OverrideMaterial.assetPath);
                    }
                    String[] MeshSplit = cptoMesh.export_properties[1].SkeletalMesh.assetPath.split("\\.");
                    CombinedMeshes.charPart5 = MeshSplit[0];
                }
            } catch (Exception e) {
                continue;
            }
        }

        String[] MeshesList = {CombinedMeshes.charPart1, CombinedMeshes.charPart2, CombinedMeshes.charPart3, CombinedMeshes.charPart4, CombinedMeshes.charPart5};

        JsonObject root = new JsonObject();
        try {
            root.addProperty("characterName", cosmeticResponse[0].name);
            JsonArray MeshArray = new JsonArray();
            root.add("Meshes", MeshArray);

            for (int i = 0; i < MeshesList.length; i++) {
                MeshArray.add(localDir + "\\UmodelExport" + MeshesList[i].replace("/", "\\") + ".psk");
            }
        } catch (Throwable e) {}

        JsonArray MaterialArray = new JsonArray();
        root.add("Materials", MaterialArray);

        JsonArray Material1, Material2, Material3, Material4, Material5, Material6, Material7, Material8;
        JsonObject Material1OBJ, Material2OBJ, Material3OBJ, Material4OBJ, Material5OBJ, Material6OBJ, Material7OBJ, Material8OBJ;

        JsonArray[] MaterialsList = {Material1 = new JsonArray(), Material2 = new JsonArray(), Material3 = new JsonArray(), Material4 = new JsonArray(),
                Material5 = new JsonArray(), Material6 = new JsonArray(), Material7 = new JsonArray(), Material8 = new JsonArray()};

        JsonObject[] MaterialsListObject = {Material1OBJ = new JsonObject(), Material2OBJ = new JsonObject(), Material3OBJ = new JsonObject(), Material4OBJ = new JsonObject(),
                Material5OBJ = new JsonObject(), Material6OBJ = new JsonObject(), Material7OBJ = new JsonObject(), Material8OBJ = new JsonObject()};

        try {
            int i = 0;
            int h = 0;
            while (i < MaterialsList.length-1) {
                pkg = fileProvider.loadGameFile(MeshesList[i] + ".uasset");
                toJson = pkg.toJson(locres);

                MeshtoMaterial meshtomat = GSON.fromJson(toJson, MeshtoMaterial.class);

                for (int e = 0; e < meshtomat.import_map.length-1; e++) {
                    if (meshtomat.import_map[e].class_name.contains("Package") && meshtomat.import_map[e].object_name.contains("Material")
                            && !meshtomat.import_map[e].object_name.contains("Engine")) {
                        h++;
                        MaterialArray.add(MaterialsListObject[h]);
                        MaterialsListObject[h].addProperty("materialPath", meshtomat.import_map[e].object_name);

                        pkg = fileProvider.loadGameFile(meshtomat.import_map[e].object_name + ".uasset");
                        toJson = pkg.toJson(locres);
                        MaterialParse matparse = GSON.fromJson(toJson, MaterialParse.class);

                        if (toJson.contains("TextureParameterValues")) {
                            for (int j = 0; j < matparse.export_properties[0].TextureParameterValues.length; j++ ) {
                                String texname = matparse.export_properties[0].TextureParameterValues[j].ParameterInfo.Name;
                                String texval = matparse.export_properties[0].TextureParameterValues[j].ParameterValue;

                                for (int g = 0; g < matparse.import_map.length-1; g++) {
                                    if (matparse.import_map[g].class_name.contains("Package") && matparse.import_map[g].object_name.contains(texval)) {
                                        MaterialsListObject[h].addProperty(texname, localDir + "\\UmodelExport" +
                                                matparse.import_map[g].object_name.replace("/", "\\") + ".tga");
                                    }
                                }
                                ;
                            }
                        }

                    }
                }
                i++;
                }

        } catch (Exception throwableitem) {
        }

        File processedFile = new File("processed.json");
        processedFile.createNewFile();

        FileWriter writer = new FileWriter(processedFile);

        writer.write(GSON.toJson(root));
        writer.close();
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

    public static class MaterialParse {
        private importedmapstuff[] import_map;
        public class importedmapstuff {
            public String class_name;
            public String object_name;
        }

        private TextureParameterValues[] export_properties;
        public class TextureParameterValues {

            public Parameters[] TextureParameterValues;
        }
        public class Parameters {
            public ParameterInfo ParameterInfo;
            public String ParameterValue;
        }
        public class ParameterInfo {
            public String Name;
        }
    }

    public static class CIDtoHID {
        private importMapSelection[] import_map;
        public class importMapSelection {
            public String class_name;
            public String object_name;
        }
    }
    public static class HIDtoHS {
        private Specializations[] export_properties;

        public class Specializations {
            public AssetPath[] Specializations;
        }
        public class AssetPath {
            public String assetPath;
        }
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
                public OverrideMaterial[] MaterialOverrides;
            }
            public class OverrideMaterial {
                public AssetPath OverrideMaterial;
            }

            public class AssetPath {
                public String assetPath;
            }
        }
    public static class MeshtoMaterial {
        private importMapSelection[] import_map;
        public class importMapSelection {
            public String class_name;
            public String object_name;
        }
    }
    public static class OverrideMaterialParse {
        private MaterialOverrides[] export_properties;

        public class MaterialOverrides {
            public AssetPath[] MaterialOverrides;
        }

        public class OverrideMaterial {
            public AssetPath OverrideMaterial;
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
    public static class AESResponse {
        public String version;
        public String mainKey;
        public Object dynamicKeys;
    }
    public static class Config {
            public String PaksDirectory = "D:\\Fortnite 14.30 Backup\\Paks";
            public Ue4Version UEVersion = Ue4Version.GAME_UE4_LATEST;
            public String EncryptionKey = "0x3440AB1D1B824905842BE1574F149F9FC7DBA2BB566993E597402B4715A28BD5";
            public boolean dumpAssets = false;
        }

    private static class CustomException extends Exception {
            public CustomException(String message) {
                super(message);
            }
        }
    }