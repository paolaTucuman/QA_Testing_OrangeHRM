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
import page_utils.ExcelReader;
import page_utils.ScreenshotHelper;

public class LoginTest extends BaseTest {

    private static final String EXCEL_PATH = ConfigReader.get("excel.path");
    private static final String SHEET_NAME = ConfigReader.get("excel.sheet.name");

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
        validarResultadoConfigurado(datos, "success");

        LoginPage loginPage = new LoginPage(driver);
        DashboardPage dashboardPage = new DashboardPage(driver);
        loginPage.login(datos.get("username"), datos.get("password"));

        Assert.assertTrue(dashboardPage.isDashboardDisplayed(),
                "TC01: no se mostro el titulo Dashboard");
        Assert.assertTrue(dashboardPage.isAuthenticatedMenuDisplayed(),
                "TC01: no se mostro el menu PIM de la sesion autenticada");
        Assert.assertTrue(loginPage.isLoginFormHidden(),
                "TC01: el formulario de Login continuo visible");

        ScreenshotHelper.takeScreenshot(driver, "TC01_Login_Exitoso_" + getCurrentBrowser());
    }

    @Test(dataProvider = "datosTC02", groups = {"automated", "negative"})
    public void tc02LoginConPasswordInvalida(Map<String, String> datos) {
        validarResultadoConfigurado(datos, "error");

        LoginPage loginPage = new LoginPage(driver);
        DashboardPage dashboardPage = new DashboardPage(driver);
        loginPage.login(datos.get("username"), datos.get("password"));

        Assert.assertEquals(loginPage.getErrorMessage(), datos.get("expectedMessage"),
                "TC02: el mensaje de error no coincide");
        Assert.assertTrue(loginPage.isLoginFormDisplayed(),
                "TC02: la prueba no permanecio en la pantalla de Login");
        Assert.assertFalse(dashboardPage.isDashboardDisplayedNow(),
                "TC02: Dashboard no debe mostrarse con una password invalida");

        ScreenshotHelper.takeScreenshot(driver,
                "TC02_Login_Password_Invalida_" + getCurrentBrowser());
    }

    private Object[][] datosDelCaso(String caseId) {
        List<Map<String, String>> filas = ExcelReader.leerDatos(EXCEL_PATH, SHEET_NAME);

        Map<String, String> caso = filas.stream()
                .filter(fila -> caseId.equalsIgnoreCase(fila.get("caseId")))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No se encontraron datos para " + caseId + " en " + EXCEL_PATH));

        return new Object[][] {{caso}};
    }

    private void validarResultadoConfigurado(Map<String, String> datos, String esperado) {
        Assert.assertEquals(datos.get("expectedResult"), esperado,
                datos.get("caseId") + ": expectedResult no coincide con el tipo de prueba");
    }
}
