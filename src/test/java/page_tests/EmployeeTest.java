package page_tests;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.testng.Assert;
import org.testng.annotations.Test;

import page_base.BaseTest;
import page_objects.DashboardPage;
import page_objects.EmployeePage;
import page_objects.LoginPage;
import page_utils.ConfigReader;
import page_utils.EvidenceReporter;
import page_utils.ScreenshotHelper;

public class EmployeeTest extends BaseTest {

    private static final DateTimeFormatter UNIQUE_SUFFIX =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Test(groups = {"automated", "positive"})
    public void tc06RegistrarEmpleadoConDatosValidos() {
        loginAsAdmin("TC06");

        String suffix = LocalDateTime.now().format(UNIQUE_SUFFIX);
        String firstName = "Auto" + suffix;
        String middleName = "QA";
        String lastName = "TC06" + suffix;

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
                "Pantalla Personal Details visible", true, personalDetailsDisplayed);
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

    @Test(groups = {"automated", "positive"})
    public void tc07BuscarEmpleadoCreadoPorId() {
        loginAsAdmin("TC07");

        String suffix = LocalDateTime.now().format(UNIQUE_SUFFIX);
        String firstName = "Auto" + suffix;
        String middleName = "QA";
        String lastName = "TC07" + suffix;

        EmployeePage employeePage = new EmployeePage(driver);
        String employeeId = employeePage.addEmployee(firstName, middleName, lastName);

        Assert.assertFalse(employeeId.isBlank(),
                "TC07: OrangeHRM no genero un Employee ID para la busqueda");

        String resultText = employeePage.searchEmployeeById(employeeId);

        boolean exactEmployeeIdFound = containsExactLine(resultText, employeeId);
        boolean firstNameFound = resultText.contains(firstName);
        boolean lastNameFound = resultText.contains(lastName);

        EvidenceReporter.recordComparison("TC07", getCurrentBrowser(),
                "Employee ID exacto en resultado", true, exactEmployeeIdFound);
        EvidenceReporter.recordComparison("TC07", getCurrentBrowser(),
                "First Name en resultado", true, firstNameFound);
        EvidenceReporter.recordComparison("TC07", getCurrentBrowser(),
                "Last Name en resultado", true, lastNameFound);

        Assert.assertTrue(exactEmployeeIdFound,
                "TC07: la fila encontrada no contiene el Employee ID exacto");
        Assert.assertTrue(firstNameFound,
                "TC07: la fila encontrada no contiene el First Name creado");
        Assert.assertTrue(lastNameFound,
                "TC07: la fila encontrada no contiene el Last Name creado");

        boolean visualStateReady = employeePage.prepareSearchEvidence(employeeId);
        String screenshotPath = ScreenshotHelper.takeScreenshot(
                driver,
                "TC07_Busqueda_Empleado_Exitoso_" + getCurrentBrowser()
        );
        EvidenceReporter.recordScreenshot(
                "TC07", getCurrentBrowser(), screenshotPath, visualStateReady
        );
    }

    @Test(groups = {"automated", "negative"})
    public void tc08ValidarFirstNameObligatorio() {
        loginAsAdmin("TC08");

        String suffix = LocalDateTime.now().format(UNIQUE_SUFFIX);
        String lastName = "TC08" + suffix;

        EmployeePage employeePage = new EmployeePage(driver);
        employeePage.attemptAddEmployeeWithoutFirstName(lastName);

        String requiredMessage = employeePage.getFirstNameRequiredMessage();
        boolean addEmployeePageDisplayed = employeePage.isOnAddEmployeePage();
        boolean personalDetailsDisplayed = employeePage.isPersonalDetailsDisplayedNow();

        EvidenceReporter.recordComparison("TC08", getCurrentBrowser(),
                "Mensaje de First Name", "Required", requiredMessage);
        EvidenceReporter.recordComparison("TC08", getCurrentBrowser(),
                "Formulario Add Employee visible", true, addEmployeePageDisplayed);
        EvidenceReporter.recordComparison("TC08", getCurrentBrowser(),
                "Personal Details visible", false, personalDetailsDisplayed);

        Assert.assertEquals(requiredMessage, "Required",
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

    private boolean containsExactLine(String text, String expectedLine) {
        return text.lines()
                .map(String::trim)
                .anyMatch(expectedLine::equals);
    }
}
