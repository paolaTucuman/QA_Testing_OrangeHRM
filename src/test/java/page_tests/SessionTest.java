package page_tests;

import java.util.List;
import java.util.Map;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import page_base.BaseTest;
import page_utils.ConfigReader;
import page_utils.ExcelReader;
import page_utils.ExecutionControl;
import test_steps.SessionSteps;

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

    @Test(dataProvider = "datosTC11", groups = {"automated", "security"})
    public void tc11LogoutYValidarSesionCerrada(Map<String, String> datos) {
        SessionSteps sesion = new SessionSteps(
                driver, getCurrentBrowser(), datos, this::registerFailureScreenshot);

        sesion.validarConfiguracionDelEscenario();
        sesion.iniciarSesionComoAdministrador();
        sesion.accederAlModuloPim();
        sesion.cerrarSesionYValidarLogin();
        sesion.guardarEvidenciaDelLogout();
        sesion.regresarConElBotonBack();
        sesion.validarQueBackNoMuestreContenidoProtegido();
        sesion.validarQueLaUrlProtegidaExijaAutenticacion();
    }

    private void validarColumnas(List<Map<String, String>> filas, String sheetName) {
        List<String> requeridas = List.of(
                "caseId", "dataId", "protectedSection",
                "expectedLogoutPage", "expectedBackAccess",
                "expectedBackPage", "expectedProtectedViewAfterBack"
        );
        if (filas.isEmpty() || !filas.get(0).keySet().containsAll(requeridas)) {
            throw new IllegalStateException(
                    "La hoja " + sheetName + " debe contener las columnas: " + requeridas);
        }
    }
}
