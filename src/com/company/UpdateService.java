package com.company;

import com.company.data.Address;
import com.company.data.FloatKeyValue;
import com.company.data.KeyValue;
import com.company.data.StringKeyValue;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Александр on 17.07.2016.
 */
public class UpdateService {
    public  static  final SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy_hh_mm");

    public static List<String> getTableNamesForUpdate() {
        ArrayList<String> tablesForUpdate = new ArrayList<String>();

        Statement statement = null;
        try {
            statement = ConnectionProvider.get().getConnection().createStatement();
            String queryString = "select tablename from for_update";
            ResultSet rs = statement.executeQuery(queryString);
            while (rs.next()) {
                tablesForUpdate.add(rs.getString(1));
            }
        } catch (SQLException e) {
            System.out.println("Error:" + e);
        } finally {
            Utils.closeQuietly(statement);
        }
        return tablesForUpdate;
    }


    public static List<String> getTableColumns(String tableName) {
        ArrayList<String> columnNames = new ArrayList<String>();

        Statement statement = null;
        try {
            statement = ConnectionProvider.get().getConnection().createStatement();
            String queryString = String.format("select COLUMN_NAME from INFORMATION_SCHEMA.COLUMNS where TABLE_NAME='%s'", tableName);
            ResultSet rs = statement.executeQuery(queryString);
            while (rs.next()) {
                columnNames.add(rs.getString(1));
            }
        } catch (SQLException e) {
            System.out.println("Error:" + e);
        } finally {
            Utils.closeQuietly(statement);
        }
        return columnNames;
    }


    public static void exportTableToFile(String tableName, String path) {
        List<String> columnNames = getTableColumns(tableName);
        System.out.println("Columns for \'" + tableName + "\':" + columnNames);
        HSSFWorkbook book = new HSSFWorkbook();
        HSSFSheet sheet = book.createSheet(tableName);
        HSSFRow row = sheet.createRow(0);
        for (int i = 0; i < columnNames.size(); i++) {
            String columnName = columnNames.get(i);
            HSSFCell cell = row.createCell(i);
            cell.setCellValue(columnName);
        }

        FileOutputStream fos = null;
        try {
            //create file and directories if needed
            File targetFile = new File(path);
            File parent = targetFile.getParentFile();
            if(!parent.exists() && !parent.mkdirs()){
                throw new IllegalStateException("Couldn't create dir: " + parent);
            }
            fos = new FileOutputStream(targetFile.getAbsolutePath());
            book.write(fos);
            book.close();
        } catch (Exception e) {
            System.out.println("Exception:" + e);
        } finally {
            Utils.closeQuietly(fos);
            Utils.closeQuietly(book);
        }
    }

    public static List<Column> getTableStructure(String tableName) {
        List<Column> results = new ArrayList<Column>();
        Statement statement = null;
        try {
            statement = ConnectionProvider.get().getConnection().createStatement();
            String queryString = String.format("select COLUMN_NAME, DATA_TYPE, IS_NULLABLE, CHARACTER_MAXIMUM_LENGTH from INFORMATION_SCHEMA.COLUMNS where TABLE_NAME='%s'", tableName);
            ResultSet rs = statement.executeQuery(queryString);
            while (rs.next()) {
                String name = rs.getString(1);
                String dataType = rs.getString(2);
                boolean isNullable = "YES".equals(rs.getString(3));
                int length = rs.getInt(4);//null -- 0
                results.add(new Column(name, dataType, DataType.VARCHAR, isNullable, length));
            }
        } catch (SQLException e) {
            System.out.println("Error:" + e);
        } finally {
            Utils.closeQuietly(statement);
        }
        return results;
    }

    public static String createTempTable(String tableName, List<Column> structure) {
        Statement statement = null;
        String tempTableName = null;
        try {
            statement = ConnectionProvider.get().getConnection().createStatement();
            tempTableName = tableName + "_" + sdf.format(new Date());

            StringBuilder cols = new StringBuilder();
            for (int i = 0; i<structure.size(); i++) {
                if (i>0) {
                    cols.append(", ");
                }

                Column column = structure.get(i);
                String dataLength = column.length > 0 ? "(" + column.length + " )" : "";
                String nullable = column.isNullable ? "NULL" : "NOT NULL";
                //Формирование части запроса на создание колонки. имя тип(размер) nullable
                cols.append(column.name).append(" ").append(column.type).append(dataLength).append(" ").append(nullable);
            }

            String createQuery = "create table " + tempTableName +" (" + cols + " )";
            statement.executeUpdate(createQuery);
        } catch (SQLException e) {
            System.out.println("Error:" + e);
        } finally {
            Utils.closeQuietly(statement);
        }
        return tempTableName;
    }

    public static void updateTable(String tableName, ArrayList<Address> data) {
        Connection connection = ConnectionProvider.get().getConnection();
        PreparedStatement ps = null;
        String sql = String.format("INSERT INTO %s (id, address, regionid) VALUES(?,?,?);", tableName);

        int count = 0;
        int batchSize = 10;
        try {
            ps = connection.prepareStatement(sql);

            for (Address address : data) {
                ps.setFloat(1, address.getId());
                ps.setString(2, address.getAddress());
                ps.setFloat(3, address.getRegionId());
                ps.addBatch();

                if (++count % batchSize == 0) {
                    ps.executeBatch();
                }
            }
            ps.executeBatch();
        } catch (SQLException e) {
            System.out.println("Error:" + e);
        } finally {
            Utils.closeQuietly(ps);
            Utils.closeQuietly(connection);
        }
    }

    public static ArrayList<KeyValue> readFromExcel(String path, String tableName, Column targetColumn) {
        ArrayList<KeyValue> result = new ArrayList<KeyValue>();
        /*
         HSSFRow row = sheet.getRow(0);
         int id = (int)row.getCell(0).getNumericCellValue();
            String name = row.getCell(1).getStringCellValue();
            int regionid = (int)row.getCell(2).getNumericCellValue();
            Address address = new Address(id, name, regionid);

            result.add(address);
        * */

        try {
            HSSFWorkbook myExcelBook = new HSSFWorkbook(new FileInputStream(path));
            HSSFSheet sheet = myExcelBook.getSheetAt(0);

            System.out.println("FirstRow=" + sheet.getFirstRowNum() + ", LastRow=" + sheet.getLastRowNum() + ", PhysicalNumber=" + sheet.getPhysicalNumberOfRows());
            int x = 0; //x - current column
//            int row = 0;
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
                HSSFCell resCell = row.getCell(res);

                int cellType = resCell != null ? resCell.getCellType() : -1;
                System.out.println("cell [" + res + ", " + y + "] type=" + cellType);

                boolean hasIdValue = idCell != null && idCell.getNumericCellValue() > 0;
                if (hasIdValue) {
                    int id = (int) (idCell.getNumericCellValue());
                    KeyValue kv = getCellValue(id, resCell, targetColumn, res, y);
                    System.out.println("kv=" + kv);
                    result.add(kv);
                } else if (resCell!=null && resCell.getStringCellValue()== null){
                    throw new RuntimeException("Row with index #" + y  + " is empty");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    private static KeyValue getCellValue(int id, HSSFCell cell, Column column, int x, int y) {
        if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
            if (!column.isNullable) {
                throw new RuntimeException("Cell[" + x + ", " + y + "] is not nullable.");
            }
            return createEmptyKeyValue(id, column);
        }

        int cellType = cell.getCellType();
        if (!correctType(cellType, column.dataType)) {
            throw new RuntimeException("Cell[" + x + ", " + y + "] type(" + cellType + ") does not correspond to dataType(" + column.dataType + ")");
        }

        KeyValue result;
        switch (cellType) {
            case Cell.CELL_TYPE_STRING:
                result = new StringKeyValue(id, cell.getStringCellValue());
                break;
            case Cell.CELL_TYPE_NUMERIC:
                result = new FloatKeyValue(id, cell.getNumericCellValue());
                break;
            default:
                throw new RuntimeException("Cell[" + x + ", " + y + "] type(" + cellType + ") does not correspond to dataType(" + column.dataType + ")");
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
            default:
                throw new RuntimeException("Unsupported type `" + column + "'!");
        }
        return result;
    }

    private static boolean correctType(int cellType, DataType dataType) {
        return Utils.dataTypesPairs.contains(new Pair<>(cellType, dataType));
    }
}




