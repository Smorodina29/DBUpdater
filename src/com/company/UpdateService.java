package com.company;

import com.company.check.Check;
import com.company.check.CheckException;
import com.company.check.ChecksHolder;
import com.company.data.*;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Александр on 17.07.2016.
 */
public class UpdateService {

    public  static  final SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy_hh_mm_ss");

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

    public static List<String> getTableColumns(String tableName, boolean forUpdate) {
        ArrayList<String> columnNames = new ArrayList<String>();

        Statement statement = null;
        try {
            statement = ConnectionProvider.get().getConnection().createStatement();
            String queryString = String.format("select COLUMN_NAME from INFORMATION_SCHEMA.COLUMNS where TABLE_NAME='%s'", tableName);
            ResultSet rs = statement.executeQuery(queryString);
            while (rs.next()) {
                String name = rs.getString(1);
                if (forUpdate || !name.equalsIgnoreCase("id")){
                    columnNames.add(name);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error:" + e);
        } finally {
            Utils.closeQuietly(statement);
        }
        return columnNames;
    }

    public static void exportTableToFile(String tableName, String path, boolean forUpdate) {
        List<String> columnNames = getTableColumns(tableName, forUpdate);
        System.out.println("Columns for export: \'" + tableName + "\':" + columnNames + ". For_update:" + forUpdate);
        HSSFWorkbook book = new HSSFWorkbook();
        HSSFRow row = book.createSheet(tableName).createRow(0);
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
                results.add(new Column(name, dataType, Utils.getType(dataType), isNullable, length));
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
            tempTableName = tableName + "_" + sdf.format(new java.util.Date());

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

    public static boolean checkForUpdate(String targetTableName, String columnName, String tempTableName, Check check, long numberOfRows) {
        boolean result = false;
        Statement statement = null;
        try {
            statement = ConnectionProvider.get().getConnection().createStatement();

            String query = check.getSqlQuery(tempTableName, targetTableName, columnName);
            System.out.println("Query:" + query);
            ResultSet rs = statement.executeQuery(query);
            rs.next();
            int count =  rs.getInt(1);
            result = check.validate(count, numberOfRows);
        } catch (SQLException e) {
            System.out.println("Error:" + e);
        } finally {
            Utils.closeQuietly(statement);
        }
        return result;
    }

    public static int updateDataFromTempToTarget(String targetTableName, String targetColumnName, String tempTableName, String tempColumnName) {
        int result = 0;
        Statement statement = null;
        try {
            statement = ConnectionProvider.get().getConnection().createStatement();
            String query = String.format("update %s set %s = u.%s from %s u join %s s on u.id = s.id", targetTableName, targetColumnName, targetColumnName, tempTableName, tempColumnName);
            System.out.println("Query for update: `" + query + "\'");
            result = statement.executeUpdate(query);
        } catch (SQLException e) {
            System.out.println("Error:" + e);
        } finally {
            Utils.closeQuietly(statement);
        }
        return result;
    }


    public static List<Column> filterForUpdate(List<Column> columns, String columnName) {
        ArrayList<Column> list = new ArrayList<>();
        for (Column column : columns) {
            if ("id".equalsIgnoreCase(column.name) || columnName.equalsIgnoreCase(column.name)) {
                list.add(column);
            }
        }
        //maybe add check result size equal 2
        return list;
    }

    public static List<Column> filterForAdd(List<Column> columns) {
        ArrayList<Column> list = new ArrayList<>();
        for (Column column : columns) {
            if (!"id".equalsIgnoreCase(column.name)) {
                list.add(column);
            }
        }
        //maybe check result size equal for columns.length-1
        return list;
    }

    public static void fillTable(String tableName, List<Map<Column, KeyValue>> data) throws SQLException {
        Connection connection = ConnectionProvider.get().getConnection();
        PreparedStatement ps = null;

        if (data.isEmpty()) {
            throw new RuntimeException("Data is empty.");
        }

        List<Column> columns = new ArrayList<>(data.get(0).keySet());

        String sql = generateSqlForInsert(columns, tableName);
        System.out.println("Generated SQL for insertion: `" + sql + "\'");
        int count = 0;
        int batchSize = 10;
        try {
            ps = connection.prepareStatement(sql);

            for (Map<Column, KeyValue> map : data) {

                for (int i = 0; i < columns.size(); i++) {
                    Column column = columns.get(i);
                    setValueFor(column, map.get(column), i + 1, ps);
                }
                ps.addBatch();
                if (++count % batchSize == 0) {
                    ps.executeBatch();
                }
            }

            ps.executeBatch();
        }  finally {
            Utils.closeQuietly(ps);
            Utils.closeQuietly(connection);
        }
    }

    public static int addDataFromTempTable(String tableName, String tempTableName, List<Column> structure) {
        Statement statement = null;
        int affected = -1;
        try {
            statement = ConnectionProvider.get().getConnection().createStatement();
            String query = generateSqlForCopingData(tableName, tempTableName, structure);
            System.out.println("Generated SQL for coping data: " + query);
            affected = statement.executeUpdate(query);
            System.out.println("Copied " + affected + " row(s).");
        } catch (SQLException e) {
            System.out.println("Error:" + e);
        } finally {
            Utils.closeQuietly(statement);
        }
        return affected;
    }

    public static String generateSqlForCopingData(String targetTable, String tempTableName, List<Column> structure) {
        StringBuilder sb = new StringBuilder();

        sb.append("INSERT INTO ").append(targetTable).append(" (");

        for (int i = 0; i < structure.size(); i++) {
            Column column = structure.get(i);
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(column.name);
        }

        sb.append(") SELECT ");


        for (int i = 0; i < structure.size(); i++) {
            Column column = structure.get(i);
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("t.").append(column.name);
        }

        sb.append(" FROM ").append(tempTableName).append(" AS t");

        return sb.toString();
    }

    private static void setValueFor(Column column, KeyValue value, int parameterIndex, PreparedStatement ps) throws SQLException {
        System.out.println("Setting `" + column.name + "' value '" + value + "'");

        if (value == null) {
            ps.setNull(parameterIndex, JDBCType.valueOf(column.dataType.name()).getVendorTypeNumber());
            return;
        }

        switch (column.dataType) {
            case VARCHAR:
                ps.setString(parameterIndex, ((StringKeyValue)value).getValue());
                break;
            case FLOAT:
                ps.setFloat(parameterIndex, ((FloatKeyValue)value).getValue());
                break;
            case DATETIME:
                ps.setDate(parameterIndex, new java.sql.Date(((DateKeyValue)value).getValue().getTime()));
                break;
            case BOOLEAN:
                ps.setBoolean(parameterIndex, ((BooleanKeyValue)value).getValue());
                break;
            default:
                throw new RuntimeException("Unsupported type for insertion: " + column.dataType);
        }
    }

    private static String generateSqlForInsert(List<Column> columns, String tableName) {
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ").append(tableName).append(" ");
        String columnNames = Utils.mkString(columns);

        String wildCards = generateWildcards(columns.size());
        sb.append("(").append(columnNames).append(") VALUES (").append(wildCards).append(")");
        return sb.toString();
    }

    private static String generateWildcards(int number) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < number; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("?");
        }
        return sb.toString();
    }

    public static Column findColumn(String columnName, List<Column> columns) {
        for (Column column : columns) {
            if (columnName.equalsIgnoreCase(column.name)) {
                return column;
            }
        }
        return null;
    }

    public static int importData(String path, String targetTableName) throws SQLException, CheckException {
        List<Column> columns = filterForAdd(getTableStructure(targetTableName));
        List<Map<Column, KeyValue>> data = FileService.readForAdd(path, targetTableName, columns);
        String tempTableName = createTempTable(targetTableName, columns);
        fillTable(tempTableName, data);

        List<Check> checks = ChecksHolder.getInstance().getChecksFor(targetTableName);
        if (checks == null || checks.isEmpty()) {
            throw new RuntimeException("Checks are not found for `" + targetTableName + "\'");
        }
        System.out.println("Found checks:" + checks);

        for (Check check : checks) {
            boolean passed = checkForUpdate(targetTableName, null, tempTableName, check,data.size());
            if (!passed) {
                switch (check.getType()) {
                    case ERROR:
                        throw new CheckException(check.getName());
                    case WARNING:
                        new Alert(Alert.AlertType.WARNING, "Предупреждение: проверка не пройдена: " + check.getName(), ButtonType.OK).show();
                        break;
                    default:
                        throw new RuntimeException("Unknown check type:" + check.getType());
                }
            } else {
                System.out.println("Passed:" + check.getName());
            }
        }

        return addDataFromTempTable(targetTableName, tempTableName, columns);
    }

    public static int importAndUpdate(String path, String targetTableName, String targetColumnName) throws SQLException, CheckException {
        List<Column> columns = filterForUpdate(getTableStructure(targetTableName), targetColumnName);

        Column targetColumn = findColumn(targetColumnName, columns);
        ArrayList<KeyValue> keyValues = FileService.readForUpdate(path, targetTableName, targetColumn);


        ArrayList<Map<Column, KeyValue>> data = new ArrayList<>();

        Column idColumn = findColumn("id", columns);
        for (KeyValue keyValue : keyValues) {
            HashMap<Column, KeyValue> map = new HashMap<>();
            map.put(idColumn, new FloatKeyValue((float) keyValue.key));//todo refactor this ugly code
            map.put(targetColumn, keyValue);
            data.add(map);
        }
        String tempTableName = createTempTable(targetTableName, columns);
        fillTable(tempTableName, data);

        System.out.println("Start checking tables!");

        List<Check> checks = ChecksHolder.getInstance().getChecksFor(targetTableName, targetColumnName);

        if (checks == null || checks.isEmpty()) {
            throw new RuntimeException("Checks are not found for `" + targetTableName + "\'");
        }
        System.out.println("Found checks:" + checks);
        int updateRowsCount = keyValues.size();
        for (Check check : checks) {
            boolean passed = checkForUpdate(targetTableName, targetColumnName, tempTableName, check, updateRowsCount);
            if (!passed) {
                switch (check.getType()) {
                    case ERROR:
                        throw new CheckException(check.getName());
                    case WARNING:
                        new Alert(Alert.AlertType.WARNING, "Предупреждение: проверка не пройдена: " + check.getName(), ButtonType.OK).show();
                        break;
                    default:
                        throw new RuntimeException("Unknown check type:" + check.getType());
                }
            } else {
                System.out.println("Passed:" + check.getName());
            }
        }
        return updateDataFromTempToTarget(targetTableName, targetColumnName, tempTableName, targetColumnName);
    }
}




