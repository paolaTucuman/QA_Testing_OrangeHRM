package page_base;

import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
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
import page_locators.LoginLocators;
import page_utils.ConfigReader;
import page_utils.JiraReporter;
import page_utils.ScreenshotHelper;

import java.lang.reflect.Method;
import java.time.Duration;

public class BaseTest {

    protected WebDriver driver;
    private String currentBrowser;

    @BeforeMethod(alwaysRun = true)
    @Parameters("browser")
    public void setUp(@Optional("chrome") String browser, Method method) {
        System.out.println("Iniciando prueba: " + method.getName());
        currentBrowser = browser.toLowerCase();
        driver = createDriver(currentBrowser);

        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);

        String baseUrl = ConfigReader.get("base.url");
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException("La propiedad base.url no esta configurada");
        }

        driver.get(baseUrl);

        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            wait.until(ExpectedConditions.visibilityOfElementLocated(LoginLocators.USERNAME));
            System.out.println("Pagina cargada: " + baseUrl);
        } catch (TimeoutException e) {
            throw new IllegalStateException(
                    "La pagina de Login no cargo dentro del tiempo esperado: " + baseUrl,
                    e
            );
        }
    }

    private WebDriver createDriver(String browser) {
        switch (browser) {
            case "chrome":
                return new ChromeDriver();
            case "firefox":
                return new FirefoxDriver();
            case "edge":
                return new EdgeDriver();
            default:
                throw new IllegalArgumentException("Navegador no soportado: " + browser);
        }
    }

    protected String getCurrentBrowser() {
        return currentBrowser;
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        try {
            if (result.getStatus() == ITestResult.FAILURE) {
                String failureReason = getFailureReason(result);
                System.out.println("Prueba fallida: " + result.getName());
                System.out.println("Razon: " + failureReason);

                String screenshotPath = ScreenshotHelper.takeScreenshot(
                        driver,
                        result.getName() + "_" + currentBrowser
                );

                try {
                    String titulo = "[AUTO] Bug en: " + result.getName();
                    String descripcion = "La prueba automatica " + result.getName() + " fallo.\n\n" +
                            "Razon: " + failureReason + "\n\n" +
                            "Navegador: " + currentBrowser + "\n" +
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
                System.out.println("Razon: " + getFailureReason(result));
            }
        } finally {
            closeDriver();
        }
    }

    private String getFailureReason(ITestResult result) {
        Throwable throwable = result.getThrowable();
        return throwable == null || throwable.getMessage() == null
                ? "Fallo sin mensaje disponible"
                : throwable.getMessage();
    }

    private void closeDriver() {
        if (driver == null) {
            return;
        }

        try {
            driver.quit();
        } catch (WebDriverException e) {
            System.out.println("No se pudo cerrar el navegador correctamente: " + e.getMessage());
        } finally {
            driver = null;
        }
    }
}
