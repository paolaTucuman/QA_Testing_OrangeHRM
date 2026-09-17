package page_utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {

    private static Properties properties;
    private static final String CONFIG_PATH = "src/test/resources/config.properties";

    static {
        try (FileInputStream fis = new FileInputStream(CONFIG_PATH)) {
            properties = new Properties();
            properties.load(fis);
            System.out.println("Configuracion cargada desde: " + CONFIG_PATH);
        } catch (IOException e) {
            System.out.println("ERROR: No se pudo cargar " + CONFIG_PATH);
            e.printStackTrace();
            properties = new Properties();
        }
    }

    public static String get(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            System.out.println("ADVERTENCIA: Propiedad no encontrada: " + key);
        }
        return value;
    }

    public static String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(properties.getProperty(key, "false"));
    }
}