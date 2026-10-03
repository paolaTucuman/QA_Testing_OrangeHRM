package page_utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelReader {

    /**
     * Lee un archivo Excel y devuelve una lista de mapas.
     * Cada mapa representa una fila, donde la clave es el nombre de la columna.
     */
    public static List<Map<String, String>> leerDatos(String filePath, String sheetName) {
        List<Map<String, String>> datos = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new IllegalStateException("No se encontro la hoja: " + sheetName);
            }

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new IllegalStateException("La hoja " + sheetName + " no tiene encabezados");
            }
            int totalColumns = headerRow.getLastCellNum();

            // Obtener los nombres de las columnas
            List<String> columnas = new ArrayList<>();
            HashSet<String> columnasUnicas = new HashSet<>();
            for (int j = 0; j < totalColumns; j++) {
                Cell cell = headerRow.getCell(j);
                String columna = cell == null ? "" : formatter.formatCellValue(cell).trim();
                if (columna.isBlank()) {
                    throw new IllegalStateException(
                            "La hoja " + sheetName + " tiene un encabezado vacio en la columna " + (j + 1));
                }
                if (!columnasUnicas.add(columna)) {
                    throw new IllegalStateException(
                            "La hoja " + sheetName + " tiene el encabezado duplicado: " + columna);
                }
                columnas.add(columna);
            }

            System.out.println("Columnas encontradas: " + columnas);

            // Leer las filas de datos
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Map<String, String> filaData = new HashMap<>();
                boolean filaVacia = true;

                for (int j = 0; j < totalColumns; j++) {
                    Cell cell = row.getCell(j);
                    String valor = cell == null ? "" : formatter.formatCellValue(cell).trim();

                    if (!valor.isEmpty()) filaVacia = false;
                    filaData.put(columnas.get(j), valor);
                }

                if (!filaVacia) {
                    datos.add(filaData);
                }
            }

            System.out.println("Total de filas leídas: " + datos.size());

        } catch (IOException e) {
            throw new IllegalStateException(
                    "No se pudo leer el Excel " + filePath + ": " + e.getMessage(), e);
        }

        return datos;
    }
}
