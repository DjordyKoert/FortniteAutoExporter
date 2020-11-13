package me.halfheart.fortniteautoexporter;

import com.google.gson.*;
import me.fungames.jfortniteparse.fileprovider.DefaultFileProvider;
import me.fungames.jfortniteparse.ue4.assets.Package;
import me.fungames.jfortniteparse.ue4.locres.FnLanguage;
import me.fungames.jfortniteparse.ue4.locres.Locres;
import me.fungames.jfortniteparse.ue4.objects.core.misc.FGuid;

import java.io.File;

import static me.halfheart.fortniteautoexporter.basicTools.createFile;



public class ItemDefinitionConversions {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Locres locres;
    private static DefaultFileProvider fileProvider;
    private static main.Config config;
    private static main.CharacterResponse cosmeticResponse;
    private static Package pkg;



    public static String CIDtoHIDPath(String source) {
        String HIDPath = "/";
        JsonObject baseJSON = GSON.fromJson(source, JsonObject.class);
        JsonArray importMapArray = baseJSON.getAsJsonArray("import_map");
        for (JsonElement pa : importMapArray) {
            JsonObject importMapObject = pa.getAsJsonObject();
            String class_name = importMapObject.get("class_name").getAsString();
            String object_name = importMapObject.get("object_name").getAsString();
            if (class_name.contains("Package") && object_name.contains("HID")) {
                HIDPath = object_name;
            }
        }
        return HIDPath;
    }

    public static String CIDtoHIDName(String source) {
        String HIDName = "/";
        JsonObject baseJSON = GSON.fromJson(source, JsonObject.class);
        JsonArray importMapArray = baseJSON.getAsJsonArray("import_map");
        for (JsonElement pa : importMapArray) {
            JsonObject importMapObject = pa.getAsJsonObject();
            String class_name = importMapObject.get("class_name").getAsString();
            String object_name = importMapObject.get("object_name").getAsString();
            if (class_name.contains("FortHeroType")) {
                HIDName = object_name;
            }
        }
        return HIDName;
    }

    public static String HIDtoHS(String source, int PathOrName) throws Exception {
        String assetPathFormat = "/";
        JsonObject baseJSON = GSON.fromJson(source, JsonObject.class);
        JsonArray exportPropertiesArray = baseJSON.getAsJsonArray("export_properties");
        for (JsonElement temp1 : exportPropertiesArray) {
            JsonObject exportPropertiesObject = temp1.getAsJsonObject();
            JsonArray specializationsArray = exportPropertiesObject.getAsJsonArray("Specializations");
            for (JsonElement temp2 : specializationsArray) {
                JsonObject specializationsObject = temp2.getAsJsonObject();
                    String assetPathUnformat = specializationsObject.get("assetPath").getAsString();
                    String[] splitparts = assetPathUnformat.split("\\.");
                    if (PathOrName == 0) {
                        assetPathFormat = splitparts[0];
                    } else if (PathOrName == 1) {
                        assetPathFormat = splitparts[1];
                    } else {
                        throw new MainException("i have no fucking clue why this happened. dm me asap LMAO.");
                }
            }
        }
        return assetPathFormat;
    }


    private static class MainException extends Exception {
        public MainException(String message) {
            super(message);
        }
    }
}
