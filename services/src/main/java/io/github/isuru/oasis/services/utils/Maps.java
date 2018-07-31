package io.github.isuru.oasis.services.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author iweerarathna
 */
public class Maps {

    public static Map<String, Object> create(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    public static MapBuilder create() {
        return new MapBuilder();
    }

    public static class MapBuilder {
        private final Map<String, Object> map;

        MapBuilder() {
            this.map = new HashMap<>();
        }

        public Map<String, Object> build() {
            return map;
        }

        public MapBuilder put(String key, Object value) {
            map.put(key, value);
            return this;
        }

    }

}