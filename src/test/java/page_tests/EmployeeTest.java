package page_tests;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

public class EmployeeTest extends BaseTest {

    private static final DateTimeFormatter UNIQUE_SUFFIX =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
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
        validarResultadoConfigurado(datos, "success");
        loginAsAdmin("TC06");

        String suffix = LocalDateTime.now().format(UNIQUE_SUFFIX);
        String firstName = construirFirstName(datos, suffix);
        String middleName = datos.get("middleName");
        String lastName = valorRequerido(datos, "lastNamePrefix") + suffix;

        EmployeePage employeePage = new EmployeePage(driver);
        String generatedEmployeeId = employeePage.addEmployee(firstName, middleName, lastName);

        String savedEmployeeId = employeePage.getEmployeeId();
        String savedFirstName = employeePage.getSavedFirstName();
        String savedMiddleName = employeePage.getSavedMiddleName();
        String savedLastName = employeePage.getSavedLastName();
        boolean personalDetailsDisplayed = employeePage.isPersonalDetailsDisplayed();

        EvidenceReporter.recordComparison("TC06", getCurrentBrowser(),
                "Employee ID generado", true, !generatedEmployeeId.isBlank());
        EvidenceReporter.recordComparison("TC06", getCurrentBrowser(),
                "Pantalla final", datos.get("expectedMessage"),
                personalDetailsDisplayed ? "Personal Details" : "");
        EvidenceReporter.recordComparison("TC06", getCurrentBrowser(),
                "Employee ID guardado", generatedEmployeeId, savedEmployeeId);
        EvidenceReporter.recordComparison("TC06", getCurrentBrowser(),
                "First Name guardado", firstName, savedFirstName);
        EvidenceReporter.recordComparison("TC06", getCurrentBrowser(),
                "Middle Name guardado", middleName, savedMiddleName);
        EvidenceReporter.recordComparison("TC06", getCurrentBrowser(),
                "Last Name guardado", lastName, savedLastName);

        Assert.assertFalse(generatedEmployeeId.isBlank(),
                "TC06: OrangeHRM no genero un Employee ID");
        Assert.assertTrue(personalDetailsDisplayed,
                "TC06: no se mostro la pantalla Personal Details");
        Assert.assertEquals(savedEmployeeId, generatedEmployeeId,
                "TC06: el Employee ID guardado no coincide con el generado");
        Assert.assertEquals(savedFirstName, firstName,
                "TC06: el First Name guardado no coincide");
        Assert.assertEquals(savedMiddleName, middleName,
                "TC06: el Middle Name guardado no coincide");
        Assert.assertEquals(savedLastName, lastName,
                "TC06: el Last Name guardado no coincide");

        boolean visualStateReady = employeePage.preparePersonalDetailsEvidence();
        String screenshotPath = ScreenshotHelper.takeScreenshot(
                driver,
                "TC06_Registro_Empleado_Exitoso_" + getCurrentBrowser()
        );
        EvidenceReporter.recordScreenshot(
                "TC06", getCurrentBrowser(), screenshotPath, visualStateReady
        );
    }

    @Test(dataProvider = "datosTC07", groups = {"automated", "positive"})
    public void tc07BuscarEmpleadoCreadoPorId(Map<String, String> datos) {
        validarResultadoConfigurado(datos, "success");
        Assert.assertEquals(valorRequerido(datos, "searchBy"), "employeeId",
                "TC07: searchBy debe ser employeeId");
        loginAsAdmin("TC07");

        String suffix = LocalDateTime.now().format(UNIQUE_SUFFIX);
        String firstName = construirFirstName(datos, suffix);
        String middleName = datos.get("middleName");
        String lastName = valorRequerido(datos, "lastNamePrefix") + suffix;

        EmployeePage employeePage = new EmployeePage(driver);
        String employeeId = employeePage.addEmployee(firstName, middleName, lastName);

        Assert.assertFalse(employeeId.isBlank(),
                "TC07: OrangeHRM no genero un Employee ID para la busqueda");

        String resultText = employeePage.searchEmployeeById(employeeId);

        boolean exactEmployeeIdFound = containsExactLine(resultText, employeeId);
        boolean firstNameFound = resultText.contains(firstName);
        boolean lastNameFound = resultText.contains(lastName);
        boolean expectedMessageFound = resultText.contains(datos.get("expectedMessage"));

        EvidenceReporter.recordComparison("TC07", getCurrentBrowser(),
                "Employee ID exacto en resultado", true, exactEmployeeIdFound);
        EvidenceReporter.recordComparison("TC07", getCurrentBrowser(),
                "First Name en resultado", true, firstNameFound);
        EvidenceReporter.recordComparison("TC07", getCurrentBrowser(),
                "Last Name en resultado", true, lastNameFound);
        EvidenceReporter.recordComparison("TC07", getCurrentBrowser(),
                "Resumen de resultados", datos.get("expectedMessage"),
                expectedMessageFound ? datos.get("expectedMessage") : resultText);

        Assert.assertTrue(exactEmployeeIdFound,
                "TC07: la fila encontrada no contiene el Employee ID exacto");
        Assert.assertTrue(firstNameFound,
                "TC07: la fila encontrada no contiene el First Name creado");
        Assert.assertTrue(lastNameFound,
                "TC07: la fila encontrada no contiene el Last Name creado");
        Assert.assertTrue(expectedMessageFound,
                "TC07: no se mostro el resumen de un registro encontrado");

        boolean visualStateReady = employeePage.prepareSearchEvidence(employeeId);
        String screenshotPath = ScreenshotHelper.takeScreenshot(
                driver,
                "TC07_Busqueda_Empleado_Exitoso_" + getCurrentBrowser()
        );
        EvidenceReporter.recordScreenshot(
                "TC07", getCurrentBrowser(), screenshotPath, visualStateReady
        );
    }

    @Test(dataProvider = "datosTC08", groups = {"automated", "negative"})
    public void tc08ValidarFirstNameObligatorio(Map<String, String> datos) {
        validarResultadoConfigurado(datos, "validation");
        loginAsAdmin("TC08");

        String suffix = LocalDateTime.now().format(UNIQUE_SUFFIX);
        String firstName = construirFirstName(datos, suffix);
        String lastName = valorRequerido(datos, "lastNamePrefix") + suffix;

        Assert.assertTrue(firstName.isBlank(),
                "TC08: firstNameMode debe producir un First Name vacio");

        EmployeePage employeePage = new EmployeePage(driver);
        employeePage.attemptAddEmployeeWithoutFirstName(lastName);

        String requiredMessage = employeePage.getFirstNameRequiredMessage();
        boolean addEmployeePageDisplayed = employeePage.isOnAddEmployeePage();
        boolean personalDetailsDisplayed = employeePage.isPersonalDetailsDisplayedNow();

        EvidenceReporter.recordComparison("TC08", getCurrentBrowser(),
                "Mensaje de First Name", datos.get("expectedMessage"), requiredMessage);
        EvidenceReporter.recordComparison("TC08", getCurrentBrowser(),
                "Formulario Add Employee visible", true, addEmployeePageDisplayed);
        EvidenceReporter.recordComparison("TC08", getCurrentBrowser(),
                "Personal Details visible", false, personalDetailsDisplayed);

        Assert.assertEquals(requiredMessage, datos.get("expectedMessage"),
                "TC08: no se mostro Required para First Name");
        Assert.assertTrue(addEmployeePageDisplayed,
                "TC08: el formulario Add Employee no permanecio visible");
        Assert.assertFalse(personalDetailsDisplayed,
                "TC08: no se debe crear un empleado sin First Name");

        boolean visualStateReady = employeePage.prepareFirstNameValidationEvidence();
        String screenshotPath = ScreenshotHelper.takeScreenshot(
                driver,
                "TC08_FirstName_Requerido_" + getCurrentBrowser()
        );
        EvidenceReporter.recordScreenshot(
                "TC08", getCurrentBrowser(), screenshotPath, visualStateReady
        );
    }

    private void loginAsAdmin(String caseId) {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login(
                ConfigReader.get("admin.username"),
                ConfigReader.get("admin.password")
        );

        DashboardPage dashboardPage = new DashboardPage(driver);
        Assert.assertTrue(dashboardPage.isDashboardDisplayed(),
                caseId + ": no fue posible iniciar sesion");
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
                "lastNamePrefix", "searchBy", "expectedResult", "expectedMessage"
        );
        if (!filas.get(0).keySet().containsAll(requeridas)) {
            throw new IllegalStateException(
                    "La hoja " + sheetName + " debe contener las columnas: " + requeridas);
        }
    }

    private String construirFirstName(Map<String, String> datos, String suffix) {
        String mode = valorRequerido(datos, "firstNameMode").toUpperCase(Locale.ROOT);
        return switch (mode) {
            case "AUTO" -> valorRequerido(datos, "firstNamePrefix") + suffix;
            case "BLANK" -> "";
            case "FIXED" -> valorRequerido(datos, "firstNamePrefix");
            default -> throw new IllegalStateException(
                    datos.get("caseId") + ": firstNameMode no soportado: " + mode);
        };
    }

    private void validarResultadoConfigurado(Map<String, String> datos, String esperado) {
        Assert.assertEquals(datos.get("expectedResult"), esperado,
                datos.get("caseId") + ": expectedResult no coincide con el tipo de prueba");
        valorRequerido(datos, "dataId");
        valorRequerido(datos, "expectedMessage");
    }

    private String valorRequerido(Map<String, String> datos, String columna) {
        String valor = datos.get(columna);
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException(
                    datos.get("caseId") + ": falta un valor para " + columna);
        }
        return valor.trim();
    }

    private boolean containsExactLine(String text, String expectedLine) {
        return text.lines()
                .map(String::trim)
                .anyMatch(expectedLine::equals);
    }
}
