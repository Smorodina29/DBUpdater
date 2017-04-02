package com.company;

import com.company.data.*;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Александр on 25.03.2017.
 */
public class FileService {
    static boolean correctType(int cellType, DataType dataType) {
        return Utils.allowedDataTypesPairs.contains(new Pair<>(cellType, dataType));
    }

    public static List<Map<Column, KeyValue>> readForAdd(String filepath, String tableName, List<Column> targetColumns) {
        ArrayList<Map<Column, KeyValue>> result = new ArrayList<>();

        try {
            HSSFWorkbook myExcelBook = new HSSFWorkbook(new FileInputStream(filepath));
            HSSFSheet sheet = myExcelBook.getSheetAt(0);

            System.out.println("FirstRow=" + sheet.getFirstRowNum() + ", LastRow=" + sheet.getLastRowNum() + ", PhysicalNumber=" + sheet.getPhysicalNumberOfRows());

            HSSFRow row = sheet.getRow(0);
            int lastX = row.getLastCellNum(); //last column
            System.out.println("LastX=" + lastX);

            List<Column> present = new ArrayList<>();
            Map<Column, Integer> col2indexMap = new HashMap<>();
            //search for columns
            int index = 0; //x - current column
            for (; index < lastX; index++) {
                System.out.println("x="+index);
                HSSFCell cell = row.getCell(index);

                if (cell == null) throw new RuntimeException("Empty header at index " + index);

                String currentHeaderName = cell.getStringCellValue(); //current value of cell
                Column found = findColumnBy(currentHeaderName, targetColumns);

                if (found == null) {
                    System.out.println("Skipped column `" + currentHeaderName + "\'");
                    continue;
                }

                if (present.contains(found)) {
                    throw new Exception("Found duplicate column with name \'" + currentHeaderName + "\'");
                }

                present.add(found);
                col2indexMap.put(found, index);
            }

            List<Column> mustPresent = filterNotNullable(targetColumns);
            if (present.isEmpty()) {
                String names = Utils.mkString(mustPresent);
                throw new RuntimeException("No columns found in file. Must present: " + names + ".");
            }

            if (!present.containsAll(mustPresent)) {
                String names = Utils.mkString(mustPresent);
                throw new Exception("One or more not nullable columns are not present. Must present: " + names + ".");
            }

            System.out.println("Columns that are present:");
            for (Column column : present) {
                System.out.println(column + " index=" + col2indexMap.get(column));
            }

            //reading from file
            int lastrow = sheet.getLastRowNum();
            int y = 1; //index of row

            for (; y<=lastrow; y++) {
                row = sheet.getRow(y);
                if (row == null) continue;//skip empty row

                HashMap<Column, KeyValue> rowData = new HashMap<>();

                for (Column column : present) {
                    Integer indexOfColumnInRow = col2indexMap.get(column);
                    HSSFCell cell = row.getCell(indexOfColumnInRow);

                    int cellType = cell != null ? cell.getCellType() : -1;
                    System.out.println("cell [" + indexOfColumnInRow + ", " + y + "] type=" + cellType);
                    rowData.put(column, getCellValue(cell, column, indexOfColumnInRow, y));
                }

                result.add(rowData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;//может возвращать еще колонки, которые присутствуют в файле?
    }

    private static KeyValue getCellValue(HSSFCell cell, Column column, int x, int y) {
        boolean isEmptyCell = cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK;
        if (isEmptyCell) {
            if (!column.isNullable) {
                throw new RuntimeException("Cell[" + x + ", " + y + "] is not nullable.");
            }
            return null;
        }

        int cellType = cell.getCellType();
        if (!correctType(cellType, column.dataType)) {
            throw new RuntimeException("Cell[" + x + ", " + y + "] type(" + cellType + ") does not correspond to dataType(" + column.dataType + ")");
        }

        KeyValue result;
        switch (column.dataType) {
            case VARCHAR:
                result = new StringKeyValue(cell.getStringCellValue());
                break;
            case FLOAT:
                result = new FloatKeyValue((float) cell.getNumericCellValue());
                break;
            case DATETIME:
                result = new DateKeyValue(cell.getDateCellValue());
                break;
            case BOOLEAN:
                String strBool = cell.getStringCellValue();
                boolean value;
                if ("true".equalsIgnoreCase(strBool)) {
                    value = true;
                } else if ("false".equalsIgnoreCase(strBool)) {
                    value = false;
                } else {
                    throw new RuntimeException("Cell[" + x + ", " + y + "] is not correct boolean value=`" + strBool + "\'");
                }
                result = new BooleanKeyValue(value);
                break;
            default:
                throw new RuntimeException("Cell[" + x + ", " + y + "] type(" + cellType + ") does not correspond to expected(" + column.dataType + ")");
        }

        return result;
    }

    private static List<Column> filterNotNullable(List<Column> targetColumns) {
        ArrayList<Column> list = new ArrayList<>();
        for (Column column : targetColumns) {
            if (!column.isNullable) {
                list.add(column);
            }
        }
        return list;
    }

    private static Column findColumnBy(String headerName, List<Column> columns) {
        for (Column column : columns) {
            if (column.name.equalsIgnoreCase(headerName)) {
                return column;
            }
        }
        return null;
    }

    static KeyValue getCellValue(int key, HSSFCell cell, Column column, int x, int y) {
        boolean isEmptyCell = cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK;
        if (isEmptyCell) {
            if (!column.isNullable) {
                throw new RuntimeException("Cell[" + x + ", " + y + "] is not nullable.");
            }
            return createEmptyKeyValue(key, column);
        }

        int cellType = cell.getCellType();
        if (!correctType(cellType, column.dataType)) {
            throw new RuntimeException("Cell[" + x + ", " + y + "] type(" + cellType + ") does not correspond to dataType(" + column.dataType + ")");
        }

        KeyValue result;
        switch (column.dataType) {
            case VARCHAR:
                result = new StringKeyValue(key, cell.getStringCellValue());
                break;
            case FLOAT:
                result = new FloatKeyValue(key, (float) cell.getNumericCellValue());
                break;
            case DATETIME:
                result = new DateKeyValue(key, cell.getDateCellValue());
                break;
            case BOOLEAN:
                String strBool = cell.getStringCellValue();
                boolean value;
                if ("true".equalsIgnoreCase(strBool)){
                    value = true;
                } else if ("false".equalsIgnoreCase(strBool)) {
                    value = false;
                } else {
                    throw new RuntimeException("Cell[" + x + ", " + y + "] is not correct boolean value=`" + strBool + "\'");
                }
                result = new BooleanKeyValue(key, value);
                break;
            default:
                throw new RuntimeException("Cell[" + x + ", " + y + "] type(" + cellType + ") does not correspond to expected(" + column.dataType + ")");
        }

        return result;
    }

    private static KeyValue createEmptyKeyValue(int id, Column column) {
        KeyValue result;
        switch (column.dataType) {
            case FLOAT:
                result = new FloatKeyValue(id, null);
                break;
            case VARCHAR:
                result = new StringKeyValue(id, null);
                break;
            case BOOLEAN:
                result = new BooleanKeyValue(id, null);
                break;
            case DATETIME:
                result = new DateKeyValue(id, null);
                break;
            default:
                throw new RuntimeException("Unsupported type `" + column + "'!");
        }
        return result;
    }

    public static ArrayList<KeyValue> readForUpdate(String path, String tableName, Column targetColumn) {
        ArrayList<KeyValue> result = new ArrayList<KeyValue>();

        try {
            HSSFWorkbook myExcelBook = new HSSFWorkbook(new FileInputStream(path));
            HSSFSheet sheet = myExcelBook.getSheetAt(0);

            System.out.println("FirstRow=" + sheet.getFirstRowNum() + ", LastRow=" + sheet.getLastRowNum() + ", PhysicalNumber=" + sheet.getPhysicalNumberOfRows());
            int x = 0; //x - current column
            int idX = -1; //idX
            int res = -1; //res - target name of column
            HSSFRow row = sheet.getRow(0);

            int lastX = row.getLastCellNum(); //last column
            System.out.println("LastX=" + lastX);

            //search for id and target column indexes
            for (; x < lastX; x++) {
                System.out.println("x="+x);
                HSSFCell cell = row.getCell(x);

                if (cell == null) throw new RuntimeException("Empty header at index " + x);

                String currentHeaderName = cell.getStringCellValue(); //current value of cell
                if ("id".equalsIgnoreCase(currentHeaderName)) {
                    if (idX > -1) {
                        throw new RuntimeException("Two ID columns");
                    } else {
                        idX = x;
                    }
                }
                if (targetColumn.name.equalsIgnoreCase(currentHeaderName)) {
                    if (res>-1) {
                        throw new RuntimeException("Two target columns");
                    }
                    else {
                        res = x;
                    }
                }
            }
            if (idX == -1 && res == -1) {
                throw new RuntimeException("Id column or target not found. idx=" + idX + ", res=" + res);
            }
            int lastrow = sheet.getLastRowNum();
            int y = 1; //index of row

            for (; y<=lastrow; y++) {
                row = sheet.getRow(y);
                if (row == null) continue;//skip empty row

                HSSFCell idCell = row.getCell(idX);
                HSSFCell targetCell = row.getCell(res);

                int cellType;
                if (targetCell != null) {
                    cellType = targetCell.getCellType();
                } else {
                    cellType = -1;
                }
                System.out.println("cell [" + res + ", " + y + "] type=" + cellType);

                boolean hasIdValue = idCell != null && idCell.getNumericCellValue() > 0;
                if (hasIdValue) {
                    int id = (int) (idCell.getNumericCellValue());
                    KeyValue kv = getCellValue(id, targetCell, targetColumn, res, y);
                    System.out.println("kv=" + kv);
                    result.add(kv);
                } else if (targetCell!=null && targetCell.getStringCellValue()!= null){
                    throw new RuntimeException("Row with index #" + y  + " has empty or incorrect id and not empty value.");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }
}
