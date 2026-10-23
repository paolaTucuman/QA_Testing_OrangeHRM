package page_tests;

import java.util.List;
import java.util.Map;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import page_base.BaseTest;
import page_utils.ConfigReader;
import page_utils.ExcelReader;
import page_utils.ExecutionControl;
import test_steps.EmployeeSteps;

public class EmployeeTest extends BaseTest {

    private static final String EXCEL_PATH = ConfigReader.get("excel.path");

    @DataProvider(name = "datosTC06")
    public Object[][] obtenerDatosTC06() {
        return datosDelCaso("TC06");
    }

    @DataProvider(name = "datosTC07")
    public Object[][] obtenerDatosTC07() {
        return datosDelCaso("TC07");
    }

    @DataProvider(name = "datosTC08")
    public Object[][] obtenerDatosTC08() {
        return datosDelCaso("TC08");
    }

    @Test(dataProvider = "datosTC06", groups = {"automated", "positive"})
    public void tc06RegistrarEmpleadoConDatosValidos(Map<String, String> datos) {
        EmployeeSteps empleado = new EmployeeSteps(driver, getCurrentBrowser(), datos);

        empleado.validarConfiguracionDeRegistroExitoso();
        empleado.iniciarSesionComoAdministrador();
        empleado.registrarEmpleadoConDatosValidos();
        empleado.validarQueElEmpleadoQuedeGuardado();
        empleado.guardarEvidenciaDelRegistro();
    }

    @Test(dataProvider = "datosTC07", groups = {"automated", "positive"})
    public void tc07BuscarEmpleadoCreadoPorId(Map<String, String> datos) {
        EmployeeSteps empleado = new EmployeeSteps(driver, getCurrentBrowser(), datos);

        empleado.validarConfiguracionDeBusquedaPorId();
        empleado.iniciarSesionComoAdministrador();
        empleado.registrarEmpleadoParaLaBusqueda();
        empleado.buscarEmpleadoCreadoPorId();
        empleado.validarQueSoloSeEncuentreElEmpleadoCreado();
        empleado.guardarEvidenciaDeLaBusqueda();
    }

    @Test(dataProvider = "datosTC08", groups = {"automated", "negative"})
    public void tc08ValidarFirstNameObligatorio(Map<String, String> datos) {
        EmployeeSteps empleado = new EmployeeSteps(driver, getCurrentBrowser(), datos);

        empleado.validarConfiguracionDeFirstNameObligatorio();
        empleado.iniciarSesionComoAdministrador();
        empleado.intentarRegistrarEmpleadoSinFirstName();
        empleado.validarQueFirstNameSeaObligatorio();
        empleado.guardarEvidenciaDeFirstNameObligatorio();
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

        System.out.println("Datos cargados: " + caso.get("dataId") + " para " + caseId);
        return new Object[][] {{caso}};
    }

    private void validarColumnas(List<Map<String, String>> filas, String sheetName) {
        if (filas.isEmpty()) {
            throw new IllegalStateException("La hoja " + sheetName + " no contiene datos");
        }

        List<String> requeridas = List.of(
                "caseId", "dataId", "firstNameMode", "firstNamePrefix", "middleName",
                "lastNamePrefix", "searchBy", "expectedResult", "expectedState"
        );
        if (!filas.get(0).keySet().containsAll(requeridas)) {
            throw new IllegalStateException(
                    "La hoja " + sheetName + " debe contener las columnas: " + requeridas);
        }
    }
}
