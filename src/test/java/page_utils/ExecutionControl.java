package page_utils;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class ExecutionControl {

    private static final String SHEET_NAME = "execution";
    private static final Set<String> REQUIRED_COLUMNS = Set.of(
            "caseId", "enabled", "chrome", "firefox", "edge", "dataSheet"
    );
    private static final Set<String> SUPPORTED_CASES = Set.of(
            "TC01", "TC02", "TC06", "TC07", "TC08"
    );
    private static final Set<String> SUPPORTED_BROWSERS = Set.of(
            "chrome", "firefox", "edge"
    );

    private static Map<String, Map<String, String>> rowsByCase;

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

        return parseBoolean(row, "enabled") && parseBoolean(row, normalizedBrowser);
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
            SUPPORTED_BROWSERS.forEach(browser -> parseBoolean(row, browser));
            requiredValue(row, "dataSheet", caseId);
        }

        if (!result.keySet().equals(SUPPORTED_CASES)) {
            throw new IllegalStateException(
                    "execution debe configurar exactamente estos casos: " + SUPPORTED_CASES);
        }
        return result;
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
