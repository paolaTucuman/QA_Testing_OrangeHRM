package page_utils;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ScreenshotHelper {

    private static final String SCREENSHOT_FOLDER = ConfigReader.get("screenshot.folder", "target/screenshots/");

    public static String takeScreenshot(WebDriver driver, String testName) {
        if (!(driver instanceof TakesScreenshot)) {
            System.out.println("No se pudo tomar la captura: driver no disponible");
            return null;
        }

        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        String safeTestName = sanitizeFileName(testName);
        String fileName = safeTestName + "_" + timestamp + ".png";
        Path screenshotFolder = Path.of(SCREENSHOT_FOLDER);
        Path destinationPath = screenshotFolder.resolve(fileName);

        try {
            Files.createDirectories(screenshotFolder);
            File sourceFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Files.copy(sourceFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
            String absolutePath = destinationPath.toAbsolutePath().toString();
            System.out.println("Captura guardada en: " + absolutePath);
            return absolutePath;
        } catch (IOException | WebDriverException e) {
            System.out.println("Error al tomar captura: " + e.getMessage());
            return null;
        }
    }

    private static String sanitizeFileName(String value) {
        String safeValue = value == null || value.isBlank() ? "screenshot" : value;
        return safeValue.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}
