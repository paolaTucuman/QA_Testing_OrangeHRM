package test_steps;

import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

import org.openqa.selenium.WebDriver;
import org.testng.Assert;

import page_objects.DashboardPage;
import page_objects.EmployeePage;
import page_objects.LoginPage;
import page_utils.ConfigReader;
import page_utils.EvidenceReporter;
import page_utils.ScreenshotHelper;

public class SessionSteps {

    private final WebDriver driver;
    private final String browser;
    private final Map<String, String> datos;
    private final Consumer<String> failureScreenshotRegistrar;
    private final LoginPage loginPage;
    private final DashboardPage dashboardPage;
    private final EmployeePage employeePage;

    private String protectedUrl;
    private String backScreenshot;
    private String actualBackPage;
    private boolean protectedViewAfterBack;

    public SessionSteps(
            WebDriver driver,
            String browser,
            Map<String, String> datos,
            Consumer<String> failureScreenshotRegistrar) {
        this.driver = driver;
        this.browser = browser;
        this.datos = datos;
        this.failureScreenshotRegistrar = failureScreenshotRegistrar;
        this.loginPage = new LoginPage(driver);
        this.dashboardPage = new DashboardPage(driver);
        this.employeePage = new EmployeePage(driver);
    }

    public void validarConfiguracionDelEscenario() {
        valorRequerido("dataId");
        Assert.assertEquals(valorRequerido("protectedSection"), "PIM",
                caseId() + ": protectedSection debe ser PIM");
        Assert.assertEquals(valorRequerido("expectedLogoutPage"), "Login",
                caseId() + ": expectedLogoutPage debe ser Login");
        Assert.assertFalse(parseBoolean("expectedBackAccess"),
                caseId() + ": se espera que la URL protegida no recupere acceso funcional");
        Assert.assertEquals(valorRequerido("expectedBackPage"), "Login",
                caseId() + ": despues de Back se espera continuar en Login");
        Assert.assertFalse(parseBoolean("expectedProtectedViewAfterBack"),
                caseId() + ": Back no debe mostrar contenido protegido desde la cache");
    }

    public void iniciarSesionComoAdministrador() {
        loginPage.login(
                ConfigReader.get("admin.username"),
                ConfigReader.get("admin.password")
        );
        Assert.assertTrue(dashboardPage.isDashboardDisplayed(),
                caseId() + ": no fue posible iniciar sesion");
    }

    public void accederAlModuloPim() {
        employeePage.goToPIM();
        protectedUrl = driver.getCurrentUrl();
        Assert.assertTrue(protectedUrl.contains("/pim/"),
                caseId() + ": no fue posible acceder a la seccion protegida PIM");
    }

    public void cerrarSesionYValidarLogin() {
        dashboardPage.logout();
        boolean loginAfterLogout = loginPage.isLoginFormDisplayedNow();
        boolean loginUrlAfterLogout = driver.getCurrentUrl().contains("/auth/login");

        EvidenceReporter.recordComparison(caseId(), browser,
                "Pantalla despues de Logout", datos.get("expectedLogoutPage"),
                loginAfterLogout ? "Login" : driver.getCurrentUrl());
        EvidenceReporter.recordComparison(caseId(), browser,
                "URL de Login despues de Logout", true, loginUrlAfterLogout);

        Assert.assertTrue(loginAfterLogout,
                caseId() + ": Logout no mostro nuevamente el formulario Login");
        Assert.assertTrue(loginUrlAfterLogout,
                caseId() + ": Logout no navego a la URL de Login");
    }

    public void guardarEvidenciaDelLogout() {
        boolean logoutEvidenceReady = loginPage.prepareLoginEvidence();
        String logoutScreenshot = ScreenshotHelper.takeScreenshot(
                driver, "TC11_Logout_Login_" + browser);
        EvidenceReporter.recordScreenshot(
                caseId(), browser, logoutScreenshot, logoutEvidenceReady);
    }

    public void regresarConElBotonBack() {
        driver.navigate().back();
        boolean backStateReady = dashboardPage.waitForSessionOrLoginState();

        String backUrl = driver.getCurrentUrl();
        boolean loginUrlAfterBack = backUrl.contains("/auth/login");
        protectedViewAfterBack = dashboardPage.isAuthenticatedNavigationDisplayedNow();
        actualBackPage = protectedViewAfterBack
                ? "PIM"
                : loginUrlAfterBack ? "Login" : backUrl;

        EvidenceReporter.recordComparison(caseId(), browser,
                "Pantalla inmediatamente despues de Back",
                valorRequerido("expectedBackPage"), actualBackPage);
        EvidenceReporter.recordComparison(caseId(), browser,
                "Contenido protegido visible despues de Back",
                parseBoolean("expectedProtectedViewAfterBack"), protectedViewAfterBack);

        backScreenshot = ScreenshotHelper.takeScreenshot(
                driver, "TC11_Logout_Back_" + browser);
        EvidenceReporter.recordScreenshot(
                caseId(), browser, backScreenshot, backStateReady);
    }

    public void validarQueBackNoMuestreContenidoProtegido() {
        try {
            Assert.assertEquals(actualBackPage, valorRequerido("expectedBackPage"),
                    caseId() + ": Back mostro una pantalla distinta de la esperada");
            Assert.assertEquals(protectedViewAfterBack,
                    parseBoolean("expectedProtectedViewAfterBack"),
                    caseId() + ": Back expuso contenido protegido desde el historial o la cache");
        } catch (AssertionError error) {
            failureScreenshotRegistrar.accept(backScreenshot);
            throw error;
        }
    }

    public void validarQueLaUrlProtegidaExijaAutenticacion() {
        driver.get(protectedUrl);
        dashboardPage.waitForSessionOrLoginState();
        boolean functionalAccessRecovered = driver.getCurrentUrl().contains("/pim/")
                && dashboardPage.isAuthenticatedNavigationDisplayedNow();
        boolean loginRequiredAgain = loginPage.isLoginFormDisplayedNow();
        boolean expectedBackAccess = parseBoolean("expectedBackAccess");

        EvidenceReporter.recordComparison(caseId(), browser,
                "Acceso funcional al solicitar la URL protegida",
                expectedBackAccess, functionalAccessRecovered);
        EvidenceReporter.recordComparison(caseId(), browser,
                "Login requerido para volver a PIM", true, loginRequiredAgain);

        Assert.assertEquals(functionalAccessRecovered, expectedBackAccess,
                caseId() + ": acceso funcional al solicitar la URL protegida. Esperado="
                        + expectedBackAccess + ", Actual=" + functionalAccessRecovered);
        Assert.assertTrue(loginRequiredAgain,
                caseId() + ": la URL protegida no exigio iniciar sesion nuevamente");
    }

    private boolean parseBoolean(String columna) {
        String valor = valorRequerido(columna).toLowerCase(Locale.ROOT);
        if ("true".equals(valor)) {
            return true;
        }
        if ("false".equals(valor)) {
            return false;
        }
        throw new IllegalStateException(
                caseId() + ": " + columna + " debe ser TRUE o FALSE");
    }

    private String valorRequerido(String columna) {
        String valor = datos.get(columna);
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException(
                    caseId() + ": falta un valor para " + columna);
        }
        return valor.trim();
    }

    private String caseId() {
        return datos.get("caseId");
    }
}
