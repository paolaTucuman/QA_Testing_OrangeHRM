package test_steps;

import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.testng.Assert;

import page_objects.DashboardPage;
import page_objects.LoginPage;
import page_utils.EvidenceReporter;
import page_utils.ScreenshotHelper;

public class LoginSteps {

    private final WebDriver driver;
    private final String browser;
    private final Map<String, String> datos;
    private final LoginPage loginPage;
    private final DashboardPage dashboardPage;

    public LoginSteps(WebDriver driver, String browser, Map<String, String> datos) {
        this.driver = driver;
        this.browser = browser;
        this.datos = datos;
        this.loginPage = new LoginPage(driver);
        this.dashboardPage = new DashboardPage(driver);
    }

    public void validarConfiguracionDeLoginExitoso() {
        validarResultadoConfigurado("success", "AUTHENTICATED");
    }

    public void validarConfiguracionDeLoginInvalido() {
        validarResultadoConfigurado("error", "LOGIN_ERROR_VISIBLE");
    }

    public void iniciarSesionConCredencialesDelCaso() {
        loginPage.login(datos.get("username"), datos.get("password"));
    }

    public void validarQueElUsuarioQuedeAutenticado() {
        boolean dashboardDisplayed = dashboardPage.isDashboardDisplayed();
        boolean authenticatedMenuDisplayed = dashboardPage.isAuthenticatedMenuDisplayed();
        boolean loginFormHidden = loginPage.isLoginFormHidden();

        EvidenceReporter.recordComparison(caseId(), browser,
                "Dashboard visible", true, dashboardDisplayed);
        EvidenceReporter.recordComparison(caseId(), browser,
                "Menu PIM de sesion autenticada visible", true, authenticatedMenuDisplayed);
        EvidenceReporter.recordComparison(caseId(), browser,
                "Formulario Login oculto", true, loginFormHidden);

        Assert.assertTrue(dashboardDisplayed,
                caseId() + ": no se mostro el titulo Dashboard");
        Assert.assertTrue(authenticatedMenuDisplayed,
                caseId() + ": no se mostro el menu PIM de la sesion autenticada");
        Assert.assertTrue(loginFormHidden,
                caseId() + ": el formulario de Login continuo visible");
    }

    public void validarQueElAccesoSeaRechazado() {
        String actualMessage = loginPage.getErrorMessage();
        boolean errorMessageVisible = !actualMessage.isBlank();
        boolean loginFormDisplayed = loginPage.isLoginFormDisplayed();
        boolean dashboardDisplayed = dashboardPage.isDashboardDisplayedNow();

        EvidenceReporter.recordComparison(caseId(), browser,
                "Alerta de autenticacion visible", true, errorMessageVisible);
        EvidenceReporter.recordComparison(caseId(), browser,
                "Formulario Login visible", true, loginFormDisplayed);
        EvidenceReporter.recordComparison(caseId(), browser,
                "Dashboard visible", false, dashboardDisplayed);

        Assert.assertTrue(errorMessageVisible,
                caseId() + ": no se mostro una alerta de autenticacion");
        Assert.assertTrue(loginFormDisplayed,
                caseId() + ": la prueba no permanecio en la pantalla de Login");
        Assert.assertFalse(dashboardDisplayed,
                caseId() + ": Dashboard no debe mostrarse con una password invalida");
    }

    public void guardarEvidenciaDeLoginExitoso() {
        guardarCaptura("TC01_Login_Exitoso_", dashboardPage.prepareDashboardEvidence());
    }

    public void guardarEvidenciaDeLoginInvalido() {
        guardarCaptura("TC02_Login_Password_Invalida_",
                loginPage.prepareInvalidLoginEvidence());
    }

    private void guardarCaptura(String nombre, boolean visualStateReady) {
        String screenshotPath = ScreenshotHelper.takeScreenshot(driver, nombre + browser);
        EvidenceReporter.recordScreenshot(
                caseId(), browser, screenshotPath, visualStateReady);
    }

    private void validarResultadoConfigurado(String resultadoEsperado, String estadoEsperado) {
        Assert.assertEquals(datos.get("expectedResult"), resultadoEsperado,
                caseId() + ": expectedResult no coincide con el tipo de prueba");
        Assert.assertEquals(datos.get("expectedState"), estadoEsperado,
                caseId() + ": expectedState no coincide con el escenario");
    }

    private String caseId() {
        return datos.get("caseId");
    }
}
