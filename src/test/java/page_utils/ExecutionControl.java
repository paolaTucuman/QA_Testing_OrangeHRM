package page_utils;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class ExecutionControl {

    private static final String SHEET_NAME = "execution";
    private static final String SETTINGS_SHEET_NAME = "settings";
    private static final Set<String> REQUIRED_COLUMNS = Set.of(
            "caseId", "enabled", "suite", "jiraReport",
            "chrome", "firefox", "edge", "dataSheet"
    );
    private static final Set<String> REQUIRED_SETTINGS_COLUMNS = Set.of(
            "property", "value"
    );
    private static final Set<String> SUPPORTED_CASES = Set.of(
            "TC01", "TC02", "TC06", "TC07", "TC08", "TC11"
    );
    private static final Set<String> SUPPORTED_BROWSERS = Set.of(
            "chrome", "firefox", "edge"
    );
    private static final Set<String> SUPPORTED_SUITES = Set.of(
            "positive", "negative", "security"
    );

    private static Map<String, Map<String, String>> rowsByCase;
    private static String runSuite;

    private ExecutionControl() {
    }

    public static synchronized boolean shouldRun(String caseId, String browser) {
        String normalizedCaseId = caseId.toUpperCase(Locale.ROOT);
        String normalizedBrowser = browser.toLowerCase(Locale.ROOT);

        if (!SUPPORTED_BROWSERS.contains(normalizedBrowser)) {
            throw new IllegalStateException("Navegador no soportado en execution: " + browser);
        }

        Map<String, String> row = getRows().get(normalizedCaseId);
        if (row == null) {
            throw new IllegalStateException(
                    "Falta configurar " + normalizedCaseId + " en la hoja " + SHEET_NAME);
        }

        String caseSuite = requiredValue(row, "suite", normalizedCaseId)
                .toLowerCase(Locale.ROOT);
        if (!SUPPORTED_SUITES.contains(caseSuite)) {
            throw new IllegalStateException(
                    normalizedCaseId + ": suite no soportada: " + caseSuite);
        }

        String selectedSuite = getRunSuite();
        boolean suiteSelected = "all".equals(selectedSuite)
                || selectedSuite.equals(caseSuite);

        return parseBoolean(row, "enabled")
                && parseBoolean(row, normalizedBrowser)
                && suiteSelected;
    }

    public static synchronized boolean shouldReportToJira(String caseId) {
        Map<String, String> row = getRows().get(caseId.toUpperCase(Locale.ROOT));
        if (row == null) {
            throw new IllegalStateException("Caso no configurado en execution: " + caseId);
        }
        return parseBoolean(row, "jiraReport");
    }

    public static synchronized String getSuite(String caseId) {
        Map<String, String> row = getRows().get(caseId.toUpperCase(Locale.ROOT));
        if (row == null) {
            throw new IllegalStateException("Caso no configurado en execution: " + caseId);
        }
        return requiredValue(row, "suite", caseId).toLowerCase(Locale.ROOT);
    }

    public static synchronized String getSelectedSuite() {
        return getRunSuite();
    }

    public static synchronized String getDataSheet(String caseId) {
        Map<String, String> row = getRows().get(caseId.toUpperCase(Locale.ROOT));
        if (row == null) {
            throw new IllegalStateException("Caso no configurado en execution: " + caseId);
        }
        return requiredValue(row, "dataSheet", caseId);
    }

    private static Map<String, Map<String, String>> getRows() {
        if (rowsByCase == null) {
            rowsByCase = loadAndValidate();
        }
        return rowsByCase;
    }

    private static Map<String, Map<String, String>> loadAndValidate() {
        String excelPath = ConfigReader.get("excel.path");
        List<Map<String, String>> rows = ExcelReader.leerDatos(excelPath, SHEET_NAME);
        if (rows.isEmpty()) {
            throw new IllegalStateException("La hoja execution no contiene casos");
        }

        if (!rows.get(0).keySet().containsAll(REQUIRED_COLUMNS)) {
            throw new IllegalStateException(
                    "La hoja execution debe contener las columnas: " + REQUIRED_COLUMNS);
        }

        Map<String, Map<String, String>> result = new HashMap<>();
        for (Map<String, String> row : rows) {
            String caseId = requiredValue(row, "caseId", "fila de execution")
                    .toUpperCase(Locale.ROOT);
            if (!SUPPORTED_CASES.contains(caseId)) {
                throw new IllegalStateException("Caso desconocido en execution: " + caseId);
            }
            if (result.put(caseId, row) != null) {
                throw new IllegalStateException("Caso duplicado en execution: " + caseId);
            }

            parseBoolean(row, "enabled");
            parseBoolean(row, "jiraReport");
            SUPPORTED_BROWSERS.forEach(browser -> parseBoolean(row, browser));
            String suite = requiredValue(row, "suite", caseId).toLowerCase(Locale.ROOT);
            if (!SUPPORTED_SUITES.contains(suite)) {
                throw new IllegalStateException(caseId + ": suite no soportada: " + suite);
            }
            requiredValue(row, "dataSheet", caseId);
        }

        if (!result.keySet().equals(SUPPORTED_CASES)) {
            throw new IllegalStateException(
                    "execution debe configurar exactamente estos casos: " + SUPPORTED_CASES);
        }
        return result;
    }

    private static String getRunSuite() {
        if (runSuite != null) {
            return runSuite;
        }

        String excelPath = ConfigReader.get("excel.path");
        List<Map<String, String>> rows = ExcelReader.leerDatos(excelPath, SETTINGS_SHEET_NAME);
        if (rows.isEmpty() || !rows.get(0).keySet().containsAll(REQUIRED_SETTINGS_COLUMNS)) {
            throw new IllegalStateException(
                    "La hoja settings debe contener las columnas: " + REQUIRED_SETTINGS_COLUMNS);
        }

        String configuredSuite = rows.stream()
                .filter(row -> "runsuite".equalsIgnoreCase(row.get("property")))
                .map(row -> requiredValue(row, "value", "settings.runSuite"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Falta la propiedad runSuite en la hoja settings"))
                .toLowerCase(Locale.ROOT);

        if (!"all".equals(configuredSuite) && !SUPPORTED_SUITES.contains(configuredSuite)) {
            throw new IllegalStateException(
                    "settings.runSuite debe ser ALL, POSITIVE, NEGATIVE o SECURITY");
        }

        runSuite = configuredSuite;
        return runSuite;
    }

    private static boolean parseBoolean(Map<String, String> row, String column) {
        String value = requiredValue(row, column, row.get("caseId"));
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value)) {
            return false;
        }
        throw new IllegalStateException(
                row.get("caseId") + ": " + column + " debe ser TRUE o FALSE");
    }

    private static String requiredValue(Map<String, String> row, String column, String context) {
        String value = row.get(column);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(context + ": falta un valor para " + column);
        }
        return value.trim();
    }
}
