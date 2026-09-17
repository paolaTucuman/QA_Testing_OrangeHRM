package page_utils;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ScreenshotHelper {

    private static final String SCREENSHOT_FOLDER = ConfigReader.get("screenshot.folder", "target/screenshots/");

    public static String takeScreenshot(WebDriver driver, String testName) {
        File folder = new File(SCREENSHOT_FOLDER);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        String fileName = testName + "_" + timestamp + ".png";
        String filePath = SCREENSHOT_FOLDER + fileName;

        try {
            File sourceFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            File destinationFile = new File(filePath);
            FileUtils.copyFile(sourceFile, destinationFile);
            System.out.println("Captura guardada en: " + filePath);
            return filePath;
        } catch (IOException e) {
            System.out.println("Error al tomar captura: " + e.getMessage());
            return null;
        }
    }
}