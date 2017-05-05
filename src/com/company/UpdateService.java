package com.company;

import com.company.check.Check;
import com.company.check.CheckException;
import com.company.data.*;
import com.company.ui.jfx.tabs.admin.models.ColumnModel;
import com.company.ui.jfx.tabs.admin.models.TableModel;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Александр on 17.07.2016.
 */
public class UpdateService {

    private static  final SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy_hh_mm_ss");

    public static Set<String> getTableNamesForAdd() throws Throwable {
        return getTableNamesBy("select distinct tablename from for_update where columnname is null;");
    }

    public static Set<String> getTableNamesForUpdate() throws Throwable {
        return getTableNamesBy("select distinct tablename from for_update where columnname is not null;");
    }

    public static Set<String> getAllTableNamesFromForUpdate() throws Throwable {
        return getTableNamesBy("select distinct tablename from for_update;");
    }

    private static Set<String> getTableNamesBy(String queryString) throws Throwable {
        Set<String> tablesForUpdate = new TreeSet<>();

        Statement statement = null;
        try {
            statement = ConnectionProvider.get().getConnection().createStatement();
            ResultSet rs = statement.executeQuery(queryString);
            while (rs.next()) {
                tablesForUpdate.add(rs.getString(1));
            }
        } finally {
            Utils.closeQuietly(statement);
        }
        return tablesForUpdate;
    }

    public static List<String> getUpdatableTableColumns(String tableName) {
        ArrayList<String> columnNames = new ArrayList<String>();

        Statement statement = null;
        try {
            statement = ConnectionProvider.get().getConnection().createStatement();
            String queryString = String.format("select columnname from for_update where tablename='%s' and columnname is not null;", tableName);
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

    public static List<String> getAllTablesNames() throws Throwable {
        ArrayList<String> names = new ArrayList<String>();

        Statement statement = null;
        try {
            statement = ConnectionProvider.get().getConnection().createStatement();
            String queryString = "SELECT ist.TABLE_NAME as table_name \n" +
                    "FROM INFORMATION_SCHEMA.TABLES ist where TABLE_NAME not in ('query_check', 'for_update', 'pair_checks');";
            ResultSet rs = statement.executeQuery(queryString);
            while (rs.next()) {
                names.add(rs.getString(1));
            }
        }  finally {
            Utils.closeQuietly(statement);
        }
        return names;
    }


    public static List<TableModel> getTablesForUpdate() throws Throwable {
        List<TableModel> tabls = new ArrayList<>();

        HashMap<String, TableModel> result = new HashMap<>();
        Statement statement = null;
        try {
            statement = ConnectionProvider.get().getConnection().createStatement();
            String queryString = "select id, tablename, columnname from for_update";
            ResultSet rs = statement.executeQuery(queryString);
            while (rs.next()) {
                String tablename = rs.getString("tablename");
                String columnname = rs.getString("columnname");

                TableModel table = result.get(tablename);

                String specialColumnKey = ColumnModel.SPECIAL_KEY;

                if (table == null) {
                    table = new TableModel(tablename);
                    table.setColumns(filterIdColumn(getAllColumnsModelsFor(tablename)));
                    table.getColumns().put(specialColumnKey, new ColumnModel(specialColumnKey, tablename));
                }

                if (columnname == null) {
                    table.setAddAllowed(true);
                    ColumnModel specialColumnForAddToTable = table.getColumns().get(specialColumnKey);
                    specialColumnForAddToTable.setEditable(true);
                    specialColumnForAddToTable.setForUpdateId(rs.getInt("id"));

                    specialColumnForAddToTable.getChecks().addAll(ChecksService.getChecksForAdd(tablename));
                    table.getColumns().put(specialColumnKey, specialColumnForAddToTable);
                } else {
                    ColumnModel column = table.getColumns().get(columnname);
                    boolean isNotFound = column == null;
                    if (isNotFound) {
                        /* in case when column was deleted from table.
                        * */
                        column = new ColumnModel(columnname, tablename);
                    }
                    column.setEditable(true);
                    column.getChecks().addAll(ChecksService.getChecksForUpdate(tablename, columnname));
                    column.setForUpdateId(rs.getInt("id"));
                }
                result.put(tablename, table);
            }
            tabls = new ArrayList<>(result.values());
        }  finally {
            Utils.closeQuietly(statement);
        }
        return tabls;
    }

    private static HashMap<String, ColumnModel> filterIdColumn(HashMap<String, ColumnModel> map) {
        map.remove("id");
        map.remove("ID");
        return map;
    }


    /**
    * returns all columns of table. isEditable is not checked
    * */
    private static HashMap<String, ColumnModel> getAllColumnsModelsFor(String tablename) {
        HashMap<String, ColumnModel> result = new HashMap<>();

        PreparedStatement statement = null;
        try {
            statement = ConnectionProvider.get().getConnection().prepareStatement("select isc.COLUMN_NAME as column_name from INFORMATION_SCHEMA.COLUMNS isc where isc.TABLE_NAME = ?");
            statement.setString(1, tablename);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                String columnName = rs.getString("column_name");
                result.put(columnName, new ColumnModel(columnName, tablename));
            }
        } catch (SQLException e) {
            System.out.println("Error:" + e);
        } finally {
            Utils.closeQuietly(statement);
        }

        return result;
    }


    public static void exportTableToFile(String tableName, String path, boolean forUpdate) {
        List<String> columnNames = getUpdatableTableColumns(tableName);
        if (forUpdate) {
            columnNames.add(0, "ID");
        }

        System.out.println("Columns for export: \'" + tableName + "\':" + columnNames + ". For updating:" + forUpdate);
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
            System.out.println("Validation " + check.getValidationMethod() + ", count=" + count + ", number of rows  = " + numberOfRows);
            result = check.validate(count, numberOfRows);
        } catch (SQLException e) {
            System.out.println("Error:" + e);
        } finally {
            Utils.closeQuietly(statement);
        }
        return result;
    }

    public static int updateDataFromTempToTarget(String targetTableName, String targetColumnName, String tempTableName) {
        int result = 0;
        Statement statement = null;
        try {
            statement = ConnectionProvider.get().getConnection().createStatement();
            String query = String.format("update %s set %s = u.%s from %s u join %s s on u.id = s.id", targetTableName, targetColumnName, targetColumnName, tempTableName, targetTableName);
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

    public static int addDataFromTempTable(String tableName, String tempTableName, List<Column> structure) throws Throwable {
        Statement statement = null;
        int affected = -1;
        try {
            statement = ConnectionProvider.get().getConnection().createStatement();
            String query = generateSqlForCopingData(tableName, tempTableName, structure);
            System.out.println("Generated SQL for coping data: " + query);
            affected = statement.executeUpdate(query);
            System.out.println("Copied " + affected + " row(s).");
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

        List<Check> checks = ChecksService.getChecksForUpdate(targetTableName, targetColumnName);

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
        return updateDataFromTempToTarget(targetTableName, targetColumnName, tempTableName);
    }

    public static void disableUpdateFor(Set<String> disableUpdateSet) throws SQLException {
        if (disableUpdateSet == null || disableUpdateSet.isEmpty()) return;
        String queryString = "delete from pair_checks where for_update_id in (select id from for_update where tablename=?);\n" +
                "delete from for_update where tablename=?;";
        PreparedStatement ps = null;
        try {
            ps = ConnectionProvider.get().getConnection().prepareStatement(queryString);
            for (String tableName : disableUpdateSet) {
                ps.setString(1, tableName);
                ps.setString(2, tableName);
                ps.executeUpdate();
            }
        } finally {
            Utils.closeQuietly(ps);
        }
    }

    public static void enableUpdateFor(Set<String> enableUpdatePatch) throws SQLException {
        if (enableUpdatePatch == null || enableUpdatePatch.isEmpty()) return;
        String queryString = "insert into for_update(tablename) select ? where not exists (select tablename from for_update where tablename=?) or not exists (select id from for_update) ;";
        PreparedStatement ps = null;
        try {
            ps = ConnectionProvider.get().getConnection().prepareStatement(queryString);
            for (String tableName : enableUpdatePatch) {
                ps.setString(1, tableName);
                ps.setString(2, tableName);
                System.out.println("Affected " + ps.executeUpdate() + " rows.");;
            }
        } finally {
            Utils.closeQuietly(ps);
        }
    }

    public static void deleteChecks(Map<ColumnModel, List<Check>> deleteCheckPatch) throws SQLException {
        if (deleteCheckPatch == null || deleteCheckPatch.isEmpty()) return;
        String queryString = "delete from pair_checks where pair_checks.for_update_id = ? and pair_checks.query_check_id = ?;";
        PreparedStatement ps = null;
        try {
            ps = ConnectionProvider.get().getConnection().prepareStatement(queryString);

            for (Map.Entry<ColumnModel, List<Check>> entry : deleteCheckPatch.entrySet()) {
                ColumnModel columnModel = entry.getKey();
                List<Check> checks = entry.getValue();

                for (Check check : checks) {
                    if (check.getId() == null || columnModel.getForUpdateId() <= 0) {
                        System.out.println("Skip deleting unknown case column=" + columnModel + " | " + check + "!");
                        continue;
                    }
                    check.getId();
                    columnModel.getForUpdateId();
                    ps.setInt(1, columnModel.getForUpdateId());
                    ps.setString(2, check.getId());
                    System.out.println("Affected " + ps.executeUpdate() + " rows.");;
                }
            }
        } finally {
            Utils.closeQuietly(ps);
        }
    }

    public static void addChecks(Map<ColumnModel, List<Check>> addCheckPatch) throws SQLException {
        if (addCheckPatch == null || addCheckPatch.isEmpty()) return;
        String queryString = "insert into pair_checks(for_update_id, query_check_id) values(?, ?);";
        PreparedStatement ps = null;
        try {
            ps = ConnectionProvider.get().getConnection().prepareStatement(queryString);

            for (Map.Entry<ColumnModel, List<Check>> entry : addCheckPatch.entrySet()) {
                ColumnModel columnModel = entry.getKey();
                List<Check> checks = entry.getValue();

                for (Check check : checks) {
                    if (check.getId() == null || columnModel.getForUpdateId() <= 0) {
                        System.out.println("Skip adding unknown case column=" + columnModel + " | " + check + "!");
                        continue;
                    }
                    check.getId();
                    columnModel.getForUpdateId();
                    ps.setInt(1, columnModel.getForUpdateId());
                    ps.setString(2, check.getId());

                    System.out.println("Affected " + ps.executeUpdate() + " rows.");;
                }
            }
        } finally {
            Utils.closeQuietly(ps);
        }
    }

    public static void disableColumns(List<ColumnModel> disableUpdatePatch) throws SQLException {
        if (disableUpdatePatch == null || disableUpdatePatch.isEmpty()) {
            return;
        }
        Connection connection = null;
        try {
            connection = ConnectionProvider.get().getConnection();
            for (ColumnModel column : disableUpdatePatch) {
                if (column.isSpecialColumnRepresentsTable()) {
                    disableAddingToTable(column.getTable(), column.getForUpdateId(), connection);
                } else {
                    disableUpdate(column, connection);
                }
            }
        } finally {
            Utils.closeQuietly(connection);
        }
    }

    private static void disableAddingToTable(String table, int forUpdateId, Connection connection) throws SQLException {
        String queryString = "delete from pair_checks where for_update_id=?;\n" +
                "delete from for_update where tablename=? and columnname is null;";

        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(queryString);
            ps.setInt(1, forUpdateId);
            ps.setString(2, table);
            System.out.println("Affected " + ps.executeUpdate() + " rows.");
            System.out.println("Disabled adding to table \'" + table + "\'");
        } finally {
            Utils.closeQuietly(ps);
        }
    }

    private static void disableUpdate(ColumnModel column, Connection connection) throws SQLException {
        String queryString = "delete from pair_checks where for_update_id=?;\n" +
                "delete from for_update where tablename=? and columnname=?;";

        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(queryString);
            ps.setInt(1, column.getForUpdateId());
            ps.setString(2, column.getTable());
            ps.setString(3, column.getName());
            ps.executeUpdate();
            System.out.println("Disabled update for " + column);
        } finally {
            Utils.closeQuietly(ps);
        }
    }

    public static void enableColumns(List<ColumnModel> enableUpdatePatch) throws SQLException {
        if (enableUpdatePatch == null || enableUpdatePatch.isEmpty()) {
            return;
        }

        Connection connection = null;
        try {
            connection = ConnectionProvider.get().getConnection();
            for (ColumnModel column : enableUpdatePatch) {
                if (column.isSpecialColumnRepresentsTable()) {
                    enableAddingToTable(column, connection);
                } else {
                    enableUpdate(column, connection);
                }
            }
        } finally {
            Utils.closeQuietly(connection);
        }
    }

    private static void enableUpdate(ColumnModel column, Connection connection) throws SQLException {
        String queryString = "insert into for_update(tablename, columnname) values (?, ?)";

        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(queryString);
            ps.setInt(1, column.getForUpdateId());
            ps.setString(2, column.getTable());
            ps.executeUpdate();
            int addedId = ps.getGeneratedKeys().getInt(1);
            column.setForUpdateId(addedId);
            System.out.println("Enabled update for " + column);
        } finally {
            Utils.closeQuietly(ps);
        }
    }

    private static void enableAddingToTable(ColumnModel column, Connection connection) throws SQLException {
        String queryString = "insert into for_update(tablename) values (?)";

        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(queryString);
            ps.setString(1, column.getTable());
            ps.executeUpdate();
            int addedId = ps.getGeneratedKeys().getInt(1);
            column.setForUpdateId(addedId);
            System.out.println("Enable update for " + column);
        } finally {
            Utils.closeQuietly(ps);
        }
    }


    public static void deleteTable(String tempTableName) throws SQLException {
        if (Utils.isEmpty(tempTableName)) {
            return;
        }
        Connection connection = null;
        Statement cs = null;
        String queryString = "IF EXISTS(select 1 from INFORMATION_SCHEMA.TABLES where TABLE_NAME='" + tempTableName + " ') drop table " + tempTableName;
        try {
            connection = ConnectionProvider.get().getConnection();
            cs = connection.createStatement();
            cs.executeUpdate(queryString);
        } finally {
            Utils.closeQuietly(cs);
            Utils.closeQuietly(connection);
        }
    }
}




