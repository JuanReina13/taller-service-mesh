package edu.uptc.swii.customerservice.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String toJson(Object object) {
        try {
            String json = objectMapper.writeValueAsString(object);
            return json;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}