package com.eulerity.hackathon.imagefinder.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {
    private static final Properties properties = new Properties();

    static {
        try (FileInputStream stream = new FileInputStream("../../resources/application.properties")) {
            properties.load(stream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load application.properties", e);
        }
    }

    /**
     * A string value of the key if exists in the application properties
     * @param key
     * @return {@code value} of the key if exists, otherwise {@code null}
     */
    public static String get(String key)  {
        return properties.getProperty(key);
    }

    /**
     * An integer value of the key if exists in the application properties
     * @param key
     * @param defaultValue
     * @return {@code int} value of the key if exists, otherwise {@code defaultValue}
     */
    public static int get(String key, int defaultValue) {
        try{
            return Integer.parseInt(properties.getProperty(key));
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
