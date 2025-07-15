package weebify.dptb2utils;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

public class ModConfigs {
    public Map<String, JsonElement> notifsMap = new LinkedHashMap<>();
    public Map<String, JsonElement> othersMap = new LinkedHashMap<>();

    public static final Map<String, JsonElement> notifsDefaultMap = new LinkedHashMap<>();
    public static final Map<String, JsonElement> othersDefaultMap = new LinkedHashMap<>();

    static <T> void createNewConfig(Map<String, JsonElement> map, Map<String, JsonElement> defaultMap, String key, String defaultValue, Class<T> clazz) {
        Type type = TypeToken.get(clazz).getType();

        try {
            T TDefaultValue = DPTB2Utils.GSON.fromJson(defaultValue, type);
            map.put(key, DPTB2Utils.GSON.toJsonTree(TDefaultValue));
            defaultMap.put(key, DPTB2Utils.GSON.toJsonTree(TDefaultValue));
        } catch (Exception e) {
            map.put(key, DPTB2Utils.GSON.toJsonTree(defaultValue));
            defaultMap.put(key, DPTB2Utils.GSON.toJsonTree(defaultValue));
        }
    }

    public ModConfigs() {
        createNewConfig(notifsMap, notifsDefaultMap, "shopUpdate", "false", Boolean.class);
        createNewConfig(notifsMap, notifsDefaultMap, "buttonMayhem", "false", Boolean.class);
        createNewConfig(notifsMap, notifsDefaultMap, "buttonDisable", "false", Boolean.class);
        createNewConfig(notifsMap, notifsDefaultMap, "bootsCollected", "false", Boolean.class);
        createNewConfig(notifsMap, notifsDefaultMap, "doorSwitch", "false", Boolean.class);

        createNewConfig(othersMap, othersDefaultMap, "autoCheer", "false", Boolean.class);
    }
}
