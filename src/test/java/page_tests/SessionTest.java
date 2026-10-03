package page_tests;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import page_base.BaseTest;
import page_objects.DashboardPage;
import page_objects.EmployeePage;
import page_objects.LoginPage;
import page_utils.ConfigReader;
import page_utils.EvidenceReporter;
import page_utils.ExcelReader;
import page_utils.ExecutionControl;
import page_utils.ScreenshotHelper;

public class SessionTest extends BaseTest {

    private static final String EXCEL_PATH = ConfigReader.get("excel.path");

    @DataProvider(name = "datosTC11")
    public Object[][] obtenerDatosTC11() {
        String sheetName = ExecutionControl.getDataSheet("TC11");
        List<Map<String, String>> filas = ExcelReader.leerDatos(EXCEL_PATH, sheetName);
        validarColumnas(filas, sheetName);

        Map<String, String> caso = filas.stream()
                .filter(fila -> "TC11".equalsIgnoreCase(fila.get("caseId")))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No se encontraron datos para TC11 en " + EXCEL_PATH));

        System.out.println("Datos cargados: " + caso.get("dataId") + " para TC11");
        return new Object[][] {{caso}};
    }

    @Test(dataProvider = "datosTC11",
            groups = {"automated", "positive", "jira-candidate"})
    public void tc11LogoutYValidarSesionCerrada(Map<String, String> datos) {
        validarDatos(datos);

        LoginPage loginPage = new LoginPage(driver);
        DashboardPage dashboardPage = new DashboardPage(driver);
        EmployeePage employeePage = new EmployeePage(driver);

        loginPage.login(
                ConfigReader.get("admin.username"),
                ConfigReader.get("admin.password")
        );
        Assert.assertTrue(dashboardPage.isDashboardDisplayed(),
                "TC11: no fue posible iniciar sesion");

        employeePage.goToPIM();
        String protectedUrl = driver.getCurrentUrl();
        Assert.assertTrue(protectedUrl.contains("/pim/"),
                "TC11: no fue posible acceder a la seccion protegida PIM");

        dashboardPage.logout();
        boolean loginAfterLogout = loginPage.isLoginFormDisplayedNow();
        boolean loginUrlAfterLogout = driver.getCurrentUrl().contains("/auth/login");

        EvidenceReporter.recordComparison("TC11", getCurrentBrowser(),
                "Pantalla despues de Logout", datos.get("expectedLogoutPage"),
                loginAfterLogout ? "Login" : driver.getCurrentUrl());
        EvidenceReporter.recordComparison("TC11", getCurrentBrowser(),
                "URL de Login despues de Logout", true, loginUrlAfterLogout);

        Assert.assertTrue(loginAfterLogout,
                "TC11: Logout no mostro nuevamente el formulario Login");
        Assert.assertTrue(loginUrlAfterLogout,
                "TC11: Logout no navego a la URL de Login");

        boolean logoutEvidenceReady = loginPage.prepareLoginEvidence();
        String logoutScreenshot = ScreenshotHelper.takeScreenshot(
                driver,
                "TC11_Logout_Login_" + getCurrentBrowser()
        );
        EvidenceReporter.recordScreenshot(
                "TC11", getCurrentBrowser(), logoutScreenshot, logoutEvidenceReady
        );

        driver.navigate().back();
        boolean backStateReady = dashboardPage.waitForSessionOrLoginState();

        driver.get(protectedUrl);
        boolean protectedRouteStateReady = dashboardPage.waitForSessionOrLoginState();
        boolean functionalAccessRecovered = driver.getCurrentUrl().contains("/pim/")
                && dashboardPage.isAuthenticatedNavigationDisplayedNow();
        boolean loginRequiredAgain = loginPage.isLoginFormDisplayedNow();
        boolean expectedBackAccess = parseBoolean(datos, "expectedBackAccess");

        EvidenceReporter.recordComparison("TC11", getCurrentBrowser(),
                "Acceso funcional recuperado despues de Back",
                expectedBackAccess, functionalAccessRecovered);
        EvidenceReporter.recordComparison("TC11", getCurrentBrowser(),
                "Login requerido para volver a PIM", true, loginRequiredAgain);

        boolean backEvidenceReady = backStateReady && protectedRouteStateReady;
        String backScreenshot = ScreenshotHelper.takeScreenshot(
                driver,
                "TC11_Logout_Back_" + getCurrentBrowser()
        );
        EvidenceReporter.recordScreenshot(
                "TC11", getCurrentBrowser(), backScreenshot, backEvidenceReady
        );

        Assert.assertEquals(functionalAccessRecovered, expectedBackAccess,
                "TC11: acceso funcional despues de Back. Esperado="
                        + expectedBackAccess + ", Actual=" + functionalAccessRecovered);
        Assert.assertTrue(loginRequiredAgain,
                "TC11: la URL protegida no exigio iniciar sesion nuevamente");
    }

    private void validarColumnas(List<Map<String, String>> filas, String sheetName) {
        List<String> requeridas = List.of(
                "caseId", "dataId", "protectedSection",
                "expectedLogoutPage", "expectedBackAccess"
        );
        if (filas.isEmpty() || !filas.get(0).keySet().containsAll(requeridas)) {
            throw new IllegalStateException(
                    "La hoja " + sheetName + " debe contener las columnas: " + requeridas);
        }
    }

    private void validarDatos(Map<String, String> datos) {
        valorRequerido(datos, "dataId");
        Assert.assertEquals(valorRequerido(datos, "protectedSection"), "PIM",
                "TC11: protectedSection debe ser PIM");
        Assert.assertEquals(valorRequerido(datos, "expectedLogoutPage"), "Login",
                "TC11: expectedLogoutPage debe ser Login");
        Assert.assertFalse(parseBoolean(datos, "expectedBackAccess"),
                "TC11: se espera que Back no recupere acceso funcional");
    }

    private boolean parseBoolean(Map<String, String> datos, String columna) {
        String valor = valorRequerido(datos, columna).toLowerCase(Locale.ROOT);
        if ("true".equals(valor)) {
            return true;
        }
        if ("false".equals(valor)) {
            return false;
        }
        throw new IllegalStateException(
                datos.get("caseId") + ": " + columna + " debe ser TRUE o FALSE");
    }

    private String valorRequerido(Map<String, String> datos, String columna) {
        String valor = datos.get(columna);
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException(
                    datos.get("caseId") + ": falta un valor para " + columna);
        }
        return valor.trim();
    }
}
