package page_tests;

import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import page_base.BaseTest;
import page_objects.DashboardPage;
import page_objects.LoginPage;
import page_utils.ConfigReader;
import page_utils.EvidenceReporter;
import page_utils.ExcelReader;
import page_utils.ExecutionControl;
import page_utils.ScreenshotHelper;

public class LoginTest extends BaseTest {

    private static final String EXCEL_PATH = ConfigReader.get("excel.path");

    @DataProvider(name = "datosTC01")
    public Object[][] obtenerDatosTC01() {
        return datosDelCaso("TC01");
    }

    @DataProvider(name = "datosTC02")
    public Object[][] obtenerDatosTC02() {
        return datosDelCaso("TC02");
    }

    @Test(dataProvider = "datosTC01", groups = {"automated", "positive"})
    public void tc01LoginConCredencialesValidas(Map<String, String> datos) {
        validarResultadoConfigurado(datos, "success", "AUTHENTICATED");

        LoginPage loginPage = new LoginPage(driver);
        DashboardPage dashboardPage = new DashboardPage(driver);
        loginPage.login(datos.get("username"), datos.get("password"));

        boolean dashboardDisplayed = dashboardPage.isDashboardDisplayed();
        boolean authenticatedMenuDisplayed = dashboardPage.isAuthenticatedMenuDisplayed();
        boolean loginFormHidden = loginPage.isLoginFormHidden();

        EvidenceReporter.recordComparison("TC01", getCurrentBrowser(),
                "Dashboard visible", true, dashboardDisplayed);
        EvidenceReporter.recordComparison("TC01", getCurrentBrowser(),
                "Menu PIM de sesion autenticada visible", true, authenticatedMenuDisplayed);
        EvidenceReporter.recordComparison("TC01", getCurrentBrowser(),
                "Formulario Login oculto", true, loginFormHidden);

        Assert.assertTrue(dashboardDisplayed,
                "TC01: no se mostro el titulo Dashboard");
        Assert.assertTrue(authenticatedMenuDisplayed,
                "TC01: no se mostro el menu PIM de la sesion autenticada");
        Assert.assertTrue(loginFormHidden,
                "TC01: el formulario de Login continuo visible");

        boolean visualStateReady = dashboardPage.prepareDashboardEvidence();
        String screenshotPath = ScreenshotHelper.takeScreenshot(
                driver,
                "TC01_Login_Exitoso_" + getCurrentBrowser()
        );
        EvidenceReporter.recordScreenshot(
                "TC01", getCurrentBrowser(), screenshotPath, visualStateReady
        );
    }

    @Test(dataProvider = "datosTC02", groups = {"automated", "negative"})
    public void tc02LoginConPasswordInvalida(Map<String, String> datos) {
        validarResultadoConfigurado(datos, "error", "LOGIN_ERROR_VISIBLE");

        LoginPage loginPage = new LoginPage(driver);
        DashboardPage dashboardPage = new DashboardPage(driver);
        loginPage.login(datos.get("username"), datos.get("password"));

        String actualMessage = loginPage.getErrorMessage();
        boolean errorMessageVisible = !actualMessage.isBlank();
        boolean loginFormDisplayed = loginPage.isLoginFormDisplayed();
        boolean dashboardDisplayed = dashboardPage.isDashboardDisplayedNow();

        EvidenceReporter.recordComparison("TC02", getCurrentBrowser(),
                "Alerta de autenticacion visible", true, errorMessageVisible);
        EvidenceReporter.recordComparison("TC02", getCurrentBrowser(),
                "Formulario Login visible", true, loginFormDisplayed);
        EvidenceReporter.recordComparison("TC02", getCurrentBrowser(),
                "Dashboard visible", false, dashboardDisplayed);

        Assert.assertTrue(errorMessageVisible,
                "TC02: no se mostro una alerta de autenticacion");
        Assert.assertTrue(loginFormDisplayed,
                "TC02: la prueba no permanecio en la pantalla de Login");
        Assert.assertFalse(dashboardDisplayed,
                "TC02: Dashboard no debe mostrarse con una password invalida");

        boolean visualStateReady = loginPage.prepareInvalidLoginEvidence();
        String screenshotPath = ScreenshotHelper.takeScreenshot(
                driver,
                "TC02_Login_Password_Invalida_" + getCurrentBrowser()
        );
        EvidenceReporter.recordScreenshot(
                "TC02", getCurrentBrowser(), screenshotPath, visualStateReady
        );
    }

    private Object[][] datosDelCaso(String caseId) {
        String sheetName = ExecutionControl.getDataSheet(caseId);
        List<Map<String, String>> filas = ExcelReader.leerDatos(EXCEL_PATH, sheetName);
        validarColumnas(filas, sheetName);

        Map<String, String> caso = filas.stream()
                .filter(fila -> caseId.equalsIgnoreCase(fila.get("caseId")))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No se encontraron datos para " + caseId + " en " + EXCEL_PATH));

        return new Object[][] {{caso}};
    }

    private void validarColumnas(List<Map<String, String>> filas, String sheetName) {
        List<String> requeridas = List.of(
                "caseId", "username", "password", "expectedResult", "expectedState"
        );
        if (filas.isEmpty() || !filas.get(0).keySet().containsAll(requeridas)) {
            throw new IllegalStateException(
                    "La hoja " + sheetName + " debe contener las columnas: " + requeridas);
        }
    }

    private void validarResultadoConfigurado(
            Map<String, String> datos, String resultadoEsperado, String estadoEsperado) {
        Assert.assertEquals(datos.get("expectedResult"), resultadoEsperado,
                datos.get("caseId") + ": expectedResult no coincide con el tipo de prueba");
        Assert.assertEquals(datos.get("expectedState"), estadoEsperado,
                datos.get("caseId") + ": expectedState no coincide con el escenario");
    }
}
