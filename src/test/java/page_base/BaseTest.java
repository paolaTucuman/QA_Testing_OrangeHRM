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
import java.util.Arrays;

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
                    if (JiraReporter.isAutomaticCreationEnabled()
                            && hasGroup(result, "jira-candidate")) {
                        String titulo = buildJiraTitle(result);
                        String descripcion = buildJiraDescription(result, failureReason);

                        String issueKey = JiraReporter.crearBug(titulo, descripcion);

                        if (issueKey != null && screenshotPath != null) {
                            JiraReporter.adjuntarCaptura(issueKey, screenshotPath);
                            JiraReporter.agregarComentario(issueKey,
                                    "Reportado automaticamente por Selenium + TestNG");
                            System.out.println("Bug reportado en JIRA: " + issueKey);
                        }
                    } else if (!hasGroup(result, "jira-candidate")) {
                        System.out.println(
                                "Reporte automatico a JIRA omitido: el caso no esta habilitado como candidato");
                    } else {
                        System.out.println(
                                "Reporte automatico a JIRA omitido: la creacion automatica esta deshabilitada");
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

    private boolean hasGroup(ITestResult result, String group) {
        return Arrays.asList(result.getMethod().getGroups()).contains(group);
    }

    private String buildJiraTitle(ITestResult result) {
        if ("tc11LogoutYValidarSesionCerrada".equals(result.getName())) {
            return "[AUTO][TC11][Seguridad] PIM visible mediante Back despues del logout";
        }
        return "[AUTO] Bug en: " + result.getName();
    }

    private String buildJiraDescription(ITestResult result, String failureReason) {
        if ("tc11LogoutYValidarSesionCerrada".equals(result.getName())) {
            return "Caso: TC11 - Validar cache despues del cierre de sesion\n\n" +
                    "Precondicion: usuario autenticado con acceso al modulo PIM.\n\n" +
                    "Pasos para reproducir:\n" +
                    "1. Iniciar sesion en OrangeHRM.\n" +
                    "2. Acceder al modulo PIM.\n" +
                    "3. Cerrar sesion.\n" +
                    "4. Presionar Back en el navegador.\n\n" +
                    "Resultado esperado: permanecer en Login sin mostrar contenido protegido.\n\n" +
                    "Resultado actual: el navegador vuelve a mostrar la pantalla PIM y contenido " +
                    "de una sesion cerrada.\n\n" +
                    "Detalle automatico: " + failureReason + "\n" +
                    "Navegador: " + currentBrowser + "\n" +
                    "URL observada: " + driver.getCurrentUrl() + "\n" +
                    "Fecha: " + new java.util.Date() + "\n\n" +
                    "La captura de evidencia se adjunta automaticamente.";
        }

        return "La prueba automatica " + result.getName() + " fallo.\n\n" +
                "Razon: " + failureReason + "\n\n" +
                "Navegador: " + currentBrowser + "\n" +
                "URL: " + ConfigReader.get("base.url") + "\n" +
                "Fecha: " + new java.util.Date();
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
