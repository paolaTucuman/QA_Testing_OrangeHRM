package page_base;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import page_utils.ConfigReader;
import page_utils.JiraReporter;
import page_utils.ScreenshotHelper;

import java.lang.reflect.Method;
import java.time.Duration;

public class BaseTest {

    protected WebDriver driver;

    @BeforeMethod
    @Parameters("browser")
    public void setUp(@Optional("chrome") String browser, Method method) {
        System.out.println("Iniciando prueba: " + method.getName());

        switch (browser.toLowerCase()) {
            case "chrome":
                WebDriverManager.chromedriver().setup();
                driver = new ChromeDriver();
                break;
            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                driver = new FirefoxDriver();
                break;
            case "edge":
                WebDriverManager.edgedriver().setup();
                driver = new EdgeDriver();
                break;
            default:
                throw new IllegalArgumentException("Navegador no soportado: " + browser);
        }

        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        String baseUrl = ConfigReader.get("base.url");
        driver.get(baseUrl);

        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
            System.out.println("Pagina cargada: " + baseUrl);
        } catch (Exception e) {
            System.out.println("La pagina tardo en cargar");
        }
    }

    @AfterMethod
    public void tearDown(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            System.out.println("Prueba fallida: " + result.getName());
            System.out.println("Razon: " + result.getThrowable().getMessage());

            String screenshotPath = ScreenshotHelper.takeScreenshot(driver, result.getName());

            try {
                String titulo = "[AUTO] Bug en: " + result.getName();
                String descripcion = "La prueba automatica " + result.getName() + " fallo.\n\n" +
                        "Razon: " + result.getThrowable().getMessage() + "\n\n" +
                        "Navegador: " + ConfigReader.get("browser") + "\n" +
                        "URL: " + ConfigReader.get("base.url") + "\n" +
                        "Fecha: " + new java.util.Date();

                String issueKey = JiraReporter.crearBug(titulo, descripcion);

                if (issueKey != null && screenshotPath != null) {
                    JiraReporter.adjuntarCaptura(issueKey, screenshotPath);
                    JiraReporter.agregarComentario(issueKey,
                            "Reportado automaticamente por Selenium + TestNG");
                    System.out.println("Bug reportado en JIRA: " + issueKey);
                }
            } catch (Exception e) {
                System.out.println("No se pudo reportar a JIRA: " + e.getMessage());
            }

        } else if (result.getStatus() == ITestResult.SUCCESS) {
            System.out.println("Prueba exitosa: " + result.getName());
        } else if (result.getStatus() == ITestResult.SKIP) {
            System.out.println("Prueba saltada: " + result.getName());
        }

        if (driver != null) {
            driver.quit();
        }
    }
}