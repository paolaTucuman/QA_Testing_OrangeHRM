package page_utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
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

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                System.out.println("No se encontró la hoja: " + sheetName);
                return datos;
            }

            Row headerRow = sheet.getRow(0);
            int totalColumns = headerRow.getLastCellNum();

            // Obtener los nombres de las columnas
            List<String> columnas = new ArrayList<>();
            for (int j = 0; j < totalColumns; j++) {
                Cell cell = headerRow.getCell(j);
                columnas.add(cell != null ? cell.getStringCellValue() : "columna_" + j);
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
                    String valor = "";

                    if (cell != null) {
                        switch (cell.getCellType()) {
                            case STRING:
                                valor = cell.getStringCellValue();
                                break;
                            case NUMERIC:
                                valor = String.valueOf((int) cell.getNumericCellValue());
                                break;
                            case BOOLEAN:
                                valor = String.valueOf(cell.getBooleanCellValue());
                                break;
                            default:
                                valor = "";
                        }
                    }

                    if (!valor.isEmpty()) filaVacia = false;
                    filaData.put(columnas.get(j), valor);
                }

                if (!filaVacia) {
                    datos.add(filaData);
                }
            }

            System.out.println("Total de filas leídas: " + datos.size());

        } catch (IOException e) {
            System.out.println("Error al leer el Excel: " + e.getMessage());
            e.printStackTrace();
        }

        return datos;
    }
}