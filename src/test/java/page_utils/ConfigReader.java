package page_utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {

    private static final Properties properties;
    private static final String CONFIG_PATH = "src/test/resources/config.properties";

    static {
        Properties loadedProperties = new Properties();
        try (FileInputStream fis = new FileInputStream(CONFIG_PATH)) {
            loadedProperties.load(fis);
            System.out.println("Configuracion cargada desde: " + CONFIG_PATH);
        } catch (IOException e) {
            System.out.println("ERROR: No se pudo cargar " + CONFIG_PATH);
            e.printStackTrace();
        }
        properties = loadedProperties;
    }

    public static String get(String key) {
        String value = resolveValue(key, null);
        if (value == null) {
            System.out.println("ADVERTENCIA: Propiedad no encontrada: " + key);
        }
        return value;
    }

    public static String get(String key, String defaultValue) {
        return resolveValue(key, defaultValue);
    }

    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key, "false"));
    }

    private static String resolveValue(String key, String defaultValue) {
        String systemValue = normalize(System.getProperty(key));
        if (systemValue != null) {
            return systemValue;
        }

        String environmentValue = normalize(System.getenv(toEnvironmentKey(key)));
        if (environmentValue != null) {
            return environmentValue;
        }

        String fileValue = normalize(properties.getProperty(key));
        return fileValue == null ? defaultValue : fileValue;
    }

    private static String toEnvironmentKey(String key) {
        return key.toUpperCase().replaceAll("[^A-Z0-9]", "_");
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
