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

        Assert.assertFalse(generatedEmployeeId.isBlank(),
                "TC06: OrangeHRM no genero un Employee ID");
        Assert.assertTrue(employeePage.isPersonalDetailsDisplayed(),
                "TC06: no se mostro la pantalla Personal Details");
        Assert.assertEquals(employeePage.getEmployeeId(), generatedEmployeeId,
                "TC06: el Employee ID guardado no coincide con el generado");
        Assert.assertEquals(employeePage.getSavedFirstName(), firstName,
                "TC06: el First Name guardado no coincide");
        Assert.assertEquals(employeePage.getSavedMiddleName(), middleName,
                "TC06: el Middle Name guardado no coincide");
        Assert.assertEquals(employeePage.getSavedLastName(), lastName,
                "TC06: el Last Name guardado no coincide");

        employeePage.preparePersonalDetailsEvidence();
        ScreenshotHelper.takeScreenshot(driver,
                "TC06_Registro_Empleado_Exitoso_" + getCurrentBrowser());
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

        Assert.assertTrue(containsExactLine(resultText, employeeId),
                "TC07: la fila encontrada no contiene el Employee ID exacto");
        Assert.assertTrue(resultText.contains(firstName),
                "TC07: la fila encontrada no contiene el First Name creado");
        Assert.assertTrue(resultText.contains(lastName),
                "TC07: la fila encontrada no contiene el Last Name creado");

        ScreenshotHelper.takeScreenshot(driver,
                "TC07_Busqueda_Empleado_Exitoso_" + getCurrentBrowser());
    }

    @Test(groups = {"automated", "negative"})
    public void tc08ValidarFirstNameObligatorio() {
        loginAsAdmin("TC08");

        String suffix = LocalDateTime.now().format(UNIQUE_SUFFIX);
        String lastName = "TC08" + suffix;

        EmployeePage employeePage = new EmployeePage(driver);
        employeePage.attemptAddEmployeeWithoutFirstName(lastName);

        Assert.assertEquals(employeePage.getFirstNameRequiredMessage(), "Required",
                "TC08: no se mostro Required para First Name");
        Assert.assertTrue(employeePage.isOnAddEmployeePage(),
                "TC08: el formulario Add Employee no permanecio visible");
        Assert.assertFalse(employeePage.isPersonalDetailsDisplayedNow(),
                "TC08: no se debe crear un empleado sin First Name");

        employeePage.prepareFirstNameValidationEvidence();
        ScreenshotHelper.takeScreenshot(driver,
                "TC08_FirstName_Requerido_" + getCurrentBrowser());
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
