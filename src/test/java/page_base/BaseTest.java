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
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import page_locators.LoginLocators;
import page_utils.ConfigReader;
import page_utils.ExecutionControl;
import page_utils.JiraReporter;
import page_utils.ScreenshotHelper;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BaseTest {

    private static final String FAILURE_SCREENSHOT_ATTRIBUTE = "failureScreenshotPath";
    private static final Pattern CASE_ID_PATTERN = Pattern.compile(
            "^tc(\\d{2})", Pattern.CASE_INSENSITIVE);

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

                String screenshotPath = getRegisteredFailureScreenshot(result);
                if (screenshotPath == null) {
                    screenshotPath = ScreenshotHelper.takeScreenshot(
                            driver,
                            result.getName() + "_" + currentBrowser
                    );
                } else {
                    System.out.println(
                            "Reutilizando captura de evidencia del escenario: " + screenshotPath);
                }

                try {
                    String caseId = getCaseId(result);
                    boolean jiraEnabledForCase = ExecutionControl.shouldReportToJira(caseId);

                    if (JiraReporter.isAutomaticCreationEnabled() && jiraEnabledForCase) {
                        String titulo = buildJiraTitle(result);
                        String descripcion = buildJiraDescription(result, failureReason);

                        String issueKey = JiraReporter.crearOReutilizarIncidencia(
                                caseId, titulo, descripcion);

                        if (issueKey != null) {
                            boolean commentAdded = JiraReporter.agregarComentario(issueKey,
                                    buildJiraExecutionComment(caseId, failureReason));
                            boolean screenshotAttached = screenshotPath != null
                                    && JiraReporter.adjuntarCaptura(issueKey, screenshotPath);

                            if (commentAdded && screenshotAttached) {
                                System.out.println(
                                        "Incidencia actualizada completamente en JIRA: " + issueKey);
                            } else {
                                System.out.println(
                                        "Incidencia localizada, pero la actualizacion quedo incompleta: "
                                                + issueKey);
                            }
                        }
                    } else if (!jiraEnabledForCase) {
                        System.out.println(
                                "Reporte automatico a JIRA omitido: jiraReport=FALSE para " + caseId);
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

    protected void registerFailureScreenshot(String screenshotPath) {
        if (screenshotPath == null || screenshotPath.isBlank()) {
            return;
        }

        ITestResult currentResult = Reporter.getCurrentTestResult();
        if (currentResult != null) {
            currentResult.setAttribute(FAILURE_SCREENSHOT_ATTRIBUTE, screenshotPath);
        }
    }

    private String getRegisteredFailureScreenshot(ITestResult result) {
        Object screenshotPath = result.getAttribute(FAILURE_SCREENSHOT_ATTRIBUTE);
        if (screenshotPath instanceof String && !((String) screenshotPath).isBlank()) {
            return (String) screenshotPath;
        }
        return null;
    }

    private String getCaseId(ITestResult result) {
        Matcher matcher = CASE_ID_PATTERN.matcher(result.getName());
        if (!matcher.find()) {
            throw new IllegalStateException(
                    "El metodo de prueba no comienza con un caseId valido: " + result.getName());
        }
        return "TC" + matcher.group(1);
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

    private String buildJiraExecutionComment(String caseId, String failureReason) {
        if ("TC11".equals(caseId)) {
            return "Ejecucion automatica de TC11.\n" +
                    "Navegador: " + currentBrowser.toLowerCase(Locale.ROOT) + "\n" +
                    "Resultado esperado: permanecer en Login y no mostrar contenido protegido.\n" +
                    "Resultado actual: Back mostro PIM desde el historial o la cache.\n" +
                    "Resultado: FALLA\n" +
                    "Detalle: " + failureReason + "\n" +
                    "URL observada: " + driver.getCurrentUrl() + "\n" +
                    "Fecha: " + new java.util.Date();
        }

        return "Evidencia automatica adicional.\n" +
                "Caso: " + caseId + "\n" +
                "Navegador: " + currentBrowser.toLowerCase(Locale.ROOT) + "\n" +
                "Resultado: FALLA\n" +
                "Detalle: " + failureReason + "\n" +
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
