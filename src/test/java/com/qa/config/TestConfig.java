package com.qa.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;

/** Reads {@code config.properties}; a system property with the same key takes precedence. */
public final class TestConfig {

    private static final String CONFIG_FILE = "config.properties";
    private static final Properties DEFAULTS = loadDefaults();

    private TestConfig() {
    }

    public static String uiBaseUrl() {
        return get("ui.baseUrl");
    }

    public static String uiUsername() {
        return get("ui.username");
    }

    public static String uiPassword() {
        return get("ui.password");
    }

    public static String browser() {
        return get("ui.browser");
    }

    public static String browserSize() {
        return get("ui.browserSize");
    }

    public static boolean headless() {
        return Boolean.parseBoolean(get("ui.headless"));
    }

    public static long uiTimeoutMs() {
        return Long.parseLong(get("ui.timeoutMs"));
    }

    public static String apiBaseUrl() {
        return get("api.baseUrl");
    }

    public static String apiUsername() {
        return get("api.username");
    }

    public static String apiPassword() {
        return get("api.password");
    }

    private static String get(String key) {
        String value = System.getProperty(key, DEFAULTS.getProperty(key));
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing configuration value: " + key);
        }
        return value.trim();
    }

    private static Properties loadDefaults() {
        Properties properties = new Properties();
        try (InputStream stream = TestConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (stream == null) {
                throw new IllegalStateException(CONFIG_FILE + " not found on the classpath");
            }
            properties.load(stream);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + CONFIG_FILE, e);
        }
        return properties;
    }
}
