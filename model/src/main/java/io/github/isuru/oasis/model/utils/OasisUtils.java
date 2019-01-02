package io.github.isuru.oasis.model.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author iweerarathna
 */
public class OasisUtils {

    public static String getEnvOr(String key, String defVal) {
        String getenv = System.getenv(key);
        if (getenv != null && !getenv.isEmpty()) {
            return getenv;
        }
        return defVal;
    }

    public static String getEnvOr(String envKey, String jvmPropKey, String defValue) {
        return getEnvOr(envKey, System.getProperty(jvmPropKey, defValue));
    }

    public static Map<String, Object> filterKeys(Properties properties, String keyPfx) {
        Map<String, Object> map = new HashMap<>();
        for (Object keyObj : properties.keySet()) {
            String key = String.valueOf(keyObj);
            if (key.startsWith(keyPfx)) {
                Object val = properties.get(key);
                String tmp = key.substring(keyPfx.length());
                map.put(tmp, val);
            }
        }
        return map;
    }

}
