package page_tests;

import java.util.List;
import java.util.Map;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import page_base.BaseTest;
import page_utils.ConfigReader;
import page_utils.ExcelReader;
import page_utils.ExecutionControl;
import test_steps.LoginSteps;

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
        LoginSteps login = new LoginSteps(driver, getCurrentBrowser(), datos);

        login.validarConfiguracionDeLoginExitoso();
        login.iniciarSesionConCredencialesDelCaso();
        login.validarQueElUsuarioQuedeAutenticado();
        login.guardarEvidenciaDeLoginExitoso();
    }

    @Test(dataProvider = "datosTC02", groups = {"automated", "negative"})
    public void tc02LoginConPasswordInvalida(Map<String, String> datos) {
        LoginSteps login = new LoginSteps(driver, getCurrentBrowser(), datos);

        login.validarConfiguracionDeLoginInvalido();
        login.iniciarSesionConCredencialesDelCaso();
        login.validarQueElAccesoSeaRechazado();
        login.guardarEvidenciaDeLoginInvalido();
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
}
