package test_steps;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.testng.Assert;

import page_objects.DashboardPage;
import page_objects.EmployeePage;
import page_objects.LoginPage;
import page_utils.ConfigReader;
import page_utils.EvidenceReporter;
import page_utils.ScreenshotHelper;

public class EmployeeSteps {

    private static final DateTimeFormatter UNIQUE_SUFFIX =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final WebDriver driver;
    private final String browser;
    private final Map<String, String> datos;
    private final EmployeePage employeePage;

    private String firstName;
    private String middleName;
    private String lastName;
    private String employeeId;
    private String resultText;
    private int resultCount;

    public EmployeeSteps(WebDriver driver, String browser, Map<String, String> datos) {
        this.driver = driver;
        this.browser = browser;
        this.datos = datos;
        this.employeePage = new EmployeePage(driver);
    }

    public void validarConfiguracionDeRegistroExitoso() {
        validarResultadoConfigurado("success", "EMPLOYEE_PROFILE");
    }

    public void validarConfiguracionDeBusquedaPorId() {
        validarResultadoConfigurado("success", "ONE_EMPLOYEE_RESULT");
        Assert.assertEquals(valorRequerido("searchBy"), "employeeId",
                caseId() + ": searchBy debe ser employeeId");
    }

    public void validarConfiguracionDeFirstNameObligatorio() {
        validarResultadoConfigurado("validation", "FIRST_NAME_VALIDATION");
    }

    public void iniciarSesionComoAdministrador() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login(
                ConfigReader.get("admin.username"),
                ConfigReader.get("admin.password")
        );

        DashboardPage dashboardPage = new DashboardPage(driver);
        Assert.assertTrue(dashboardPage.isDashboardDisplayed(),
                caseId() + ": no fue posible iniciar sesion");
    }

    public void registrarEmpleadoConDatosValidos() {
        prepararDatosUnicos();
        employeeId = employeePage.addEmployee(firstName, middleName, lastName);
    }

    public void validarQueElEmpleadoQuedeGuardado() {
        String savedEmployeeId = employeePage.getEmployeeId();
        String savedFirstName = employeePage.getSavedFirstName();
        String savedMiddleName = employeePage.getSavedMiddleName();
        String savedLastName = employeePage.getSavedLastName();
        boolean personalDetailsDisplayed = employeePage.isPersonalDetailsDisplayed();

        EvidenceReporter.recordComparison(caseId(), browser,
                "Employee ID generado", true, !employeeId.isBlank());
        EvidenceReporter.recordComparison(caseId(), browser,
                "Estado final", datos.get("expectedState"),
                personalDetailsDisplayed ? "EMPLOYEE_PROFILE" : "UNKNOWN");
        EvidenceReporter.recordComparison(caseId(), browser,
                "Employee ID guardado", employeeId, savedEmployeeId);
        EvidenceReporter.recordComparison(caseId(), browser,
                "First Name guardado", firstName, savedFirstName);
        EvidenceReporter.recordComparison(caseId(), browser,
                "Middle Name guardado", middleName, savedMiddleName);
        EvidenceReporter.recordComparison(caseId(), browser,
                "Last Name guardado", lastName, savedLastName);

        Assert.assertFalse(employeeId.isBlank(),
                caseId() + ": OrangeHRM no genero un Employee ID");
        Assert.assertTrue(personalDetailsDisplayed,
                caseId() + ": no se mostro la pantalla Personal Details");
        Assert.assertEquals(savedEmployeeId, employeeId,
                caseId() + ": el Employee ID guardado no coincide con el generado");
        Assert.assertEquals(savedFirstName, firstName,
                caseId() + ": el First Name guardado no coincide");
        Assert.assertEquals(savedMiddleName, middleName,
                caseId() + ": el Middle Name guardado no coincide");
        Assert.assertEquals(savedLastName, lastName,
                caseId() + ": el Last Name guardado no coincide");
    }

    public void registrarEmpleadoParaLaBusqueda() {
        prepararDatosUnicos();
        employeeId = employeePage.addEmployee(firstName, middleName, lastName);
        Assert.assertFalse(employeeId.isBlank(),
                caseId() + ": OrangeHRM no genero un Employee ID para la busqueda");
    }

    public void buscarEmpleadoCreadoPorId() {
        resultText = employeePage.searchEmployeeById(employeeId);
        resultCount = employeePage.getSearchResultCount();
    }

    public void validarQueSoloSeEncuentreElEmpleadoCreado() {
        boolean exactEmployeeIdFound = containsExactLine(resultText, employeeId);
        boolean firstNameFound = resultText.contains(firstName);
        boolean lastNameFound = resultText.contains(lastName);

        EvidenceReporter.recordComparison(caseId(), browser,
                "Employee ID exacto en resultado", true, exactEmployeeIdFound);
        EvidenceReporter.recordComparison(caseId(), browser,
                "First Name en resultado", true, firstNameFound);
        EvidenceReporter.recordComparison(caseId(), browser,
                "Last Name en resultado", true, lastNameFound);
        EvidenceReporter.recordComparison(caseId(), browser,
                "Cantidad de resultados", 1, resultCount);

        Assert.assertTrue(exactEmployeeIdFound,
                caseId() + ": la fila encontrada no contiene el Employee ID exacto");
        Assert.assertTrue(firstNameFound,
                caseId() + ": la fila encontrada no contiene el First Name creado");
        Assert.assertTrue(lastNameFound,
                caseId() + ": la fila encontrada no contiene el Last Name creado");
        Assert.assertEquals(resultCount, 1,
                caseId() + ": la busqueda debe devolver exactamente un empleado");
    }

    public void intentarRegistrarEmpleadoSinFirstName() {
        prepararDatosUnicos();
        Assert.assertTrue(firstName.isBlank(),
                caseId() + ": firstNameMode debe producir un First Name vacio");
        employeePage.attemptAddEmployeeWithoutFirstName(lastName);
    }

    public void validarQueFirstNameSeaObligatorio() {
        String requiredMessage = employeePage.getFirstNameRequiredMessage();
        boolean firstNameValidationVisible = !requiredMessage.isBlank();
        boolean addEmployeePageDisplayed = employeePage.isOnAddEmployeePage();
        boolean personalDetailsDisplayed = employeePage.isPersonalDetailsDisplayedNow();

        EvidenceReporter.recordComparison(caseId(), browser,
                "Validacion asociada a First Name", true, firstNameValidationVisible);
        EvidenceReporter.recordComparison(caseId(), browser,
                "Formulario Add Employee visible", true, addEmployeePageDisplayed);
        EvidenceReporter.recordComparison(caseId(), browser,
                "Personal Details visible", false, personalDetailsDisplayed);

        Assert.assertTrue(firstNameValidationVisible,
                caseId() + ": no se mostro la validacion asociada a First Name");
        Assert.assertTrue(addEmployeePageDisplayed,
                caseId() + ": el formulario Add Employee no permanecio visible");
        Assert.assertFalse(personalDetailsDisplayed,
                caseId() + ": no se debe crear un empleado sin First Name");
    }

    public void guardarEvidenciaDelRegistro() {
        guardarCaptura("TC06_Registro_Empleado_Exitoso_",
                employeePage.preparePersonalDetailsEvidence());
    }

    public void guardarEvidenciaDeLaBusqueda() {
        guardarCaptura("TC07_Busqueda_Empleado_Exitoso_",
                employeePage.prepareSearchEvidence(employeeId));
    }

    public void guardarEvidenciaDeFirstNameObligatorio() {
        guardarCaptura("TC08_FirstName_Requerido_",
                employeePage.prepareFirstNameValidationEvidence());
    }

    private void prepararDatosUnicos() {
        String suffix = LocalDateTime.now().format(UNIQUE_SUFFIX);
        firstName = construirFirstName(suffix);
        middleName = datos.get("middleName");
        lastName = valorRequerido("lastNamePrefix") + suffix;
    }

    private String construirFirstName(String suffix) {
        String mode = valorRequerido("firstNameMode").toUpperCase(Locale.ROOT);
        return switch (mode) {
            case "AUTO" -> valorRequerido("firstNamePrefix") + suffix;
            case "BLANK" -> "";
            case "FIXED" -> valorRequerido("firstNamePrefix");
            default -> throw new IllegalStateException(
                    caseId() + ": firstNameMode no soportado: " + mode);
        };
    }

    private void validarResultadoConfigurado(String resultadoEsperado, String estadoEsperado) {
        Assert.assertEquals(datos.get("expectedResult"), resultadoEsperado,
                caseId() + ": expectedResult no coincide con el tipo de prueba");
        valorRequerido("dataId");
        Assert.assertEquals(valorRequerido("expectedState"), estadoEsperado,
                caseId() + ": expectedState no coincide con el escenario");
    }

    private String valorRequerido(String columna) {
        String valor = datos.get(columna);
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException(
                    caseId() + ": falta un valor para " + columna);
        }
        return valor.trim();
    }

    private boolean containsExactLine(String text, String expectedLine) {
        return text.lines()
                .map(String::trim)
                .anyMatch(expectedLine::equals);
    }

    private void guardarCaptura(String nombre, boolean visualStateReady) {
        String screenshotPath = ScreenshotHelper.takeScreenshot(driver, nombre + browser);
        EvidenceReporter.recordScreenshot(
                caseId(), browser, screenshotPath, visualStateReady);
    }

    private String caseId() {
        return datos.get("caseId");
    }
}
