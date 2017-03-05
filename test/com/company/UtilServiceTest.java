package com.company;

import com.company.check.PresentRowsCheck;
import com.company.check.RowCountCheck;
import com.company.check.UniqueRowsCheck;
import com.company.data.*;
import org.junit.Test;

import java.sql.SQLException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Александр on 17.07.2016.
 */
public class UtilServiceTest {
    @Test
    public void tablesForUpdate() throws Exception {
        List<String> expected = Arrays.asList("address", "distributors", "regions", "salepoint", "tradenet", "users", "warehouse");
        List<String> actual = UpdateService.getTableNamesForUpdate();
        assertEquals(actual, expected);
    }

    @Test
    public void getColumnNames() throws Exception {
        String tableName = "address";
        List<String> expected = Arrays.asList("id", "address", "regionid");
        List<String> actual = UpdateService.getTableColumns(tableName, true);
        assertEquals(actual, expected);
    }

    @Test
    public void getTableStructure() throws Exception {

    }

    @Test
    public void exportTableToFile() {
        String tableName = "address";
        String path = "C:\\tmp\\1.xls";
        UpdateService.exportTableToFile(tableName, path, true);
    }

    @Test
    public void tableStructure() {
        String tableName = "address";
        List<Column> columns = UpdateService.getTableStructure(tableName);
        for (Column column : columns) {
            System.out.println(column);
        }
    }

    @Test
    public void createTempTable() {
        String originalTableName = "address";
        List<Column> columns = UpdateService.getTableStructure(originalTableName);
        UpdateService.createTempTable(originalTableName, columns);
    }

    @Test
    public void fillTempTable() {
        //todo delete me
        ArrayList<Address> data = new ArrayList<>();
        data.add(new Address(1, "Nevskiy prospect, 1", 2));
        data.add(new Address(2, "Lenina st, 2", 1));
        data.add(new Address(3, "Moskovskaya, 17 ", 5));
        data.add(new Address(2, "Lenina st, 3", 1));

        String originalTableName = "address";
        List<Column> structure = UpdateService.getTableStructure(originalTableName);
        String tempTableName = UpdateService.createTempTable(originalTableName, structure);
        UpdateService.updateTable(tempTableName, data);
    }

    @Test
    public void readDataFromFile() {
        ArrayList<KeyValue> expected = new ArrayList<>();
        expected.add(new StringKeyValue(1, "Nevskiy prospect, 1"));
        expected.add(new StringKeyValue(2, "Lenina st, 2"));
        expected.add(new StringKeyValue(3, "Moskovskaya, 17 "));
        expected.add(new StringKeyValue(2, "Lenina st, 3"));

        String tableName = "address";
        String path = "test/resources/address.xls";
        Column targetColumn = new Column("address", "varchar", DataType.VARCHAR, false, 50);
        List<KeyValue> actual = UpdateService.readFromExcel(path, tableName, targetColumn);
        assertEquals(expected, actual);
    }

    @Test
    public void readIntDataFromFile() {
        ArrayList<KeyValue> expected = new ArrayList<>();
        expected.add(new FloatKeyValue(1, 2f));
        expected.add(new FloatKeyValue(2, 1f));
        expected.add(new FloatKeyValue(3, 5f));
        expected.add(new FloatKeyValue(2, 1f));

        String tableName = "address";
        String path = "test/resources/address.xls";
        Column targetColumn = new Column("regionid", "int", DataType.FLOAT, true, 0);
        List<KeyValue> actual = UpdateService.readFromExcel(path, tableName, targetColumn);
        assertEquals(expected, actual);
    }

    @Test
    public void readDateDataFromExcel() {
        ArrayList<KeyValue> expected = new ArrayList<>();
        ;
        expected.add(new DateKeyValue(1, date(2016, 10, 15)));
        expected.add(new DateKeyValue(2, date(2019, 0, 25)));
        expected.add(new DateKeyValue(3, date(2016, 4, 9)));
        expected.add(new DateKeyValue(2, date(2016, 11, 12)));

        String tableName = "address";
        String path = "test/resources/address.xls";
        Column targetColumn = new Column("date", "datetime", DataType.DATETIME, true, 0);
        List<KeyValue> actual = UpdateService.readFromExcel(path, tableName, targetColumn);
        assertEquals(expected, actual);

    }

    @Test
    public void readBoolDataFromExcel() {
        ArrayList<KeyValue> expected = new ArrayList<>();
        expected.add(new BooleanKeyValue(1, true));
        expected.add(new BooleanKeyValue(2, false));
        expected.add(new BooleanKeyValue(3, true));
        expected.add(new BooleanKeyValue(2, true));

        String tableName = "address";
        String path = "test/resources/address.xls";
        Column targetColumn = new Column("isTradenet", "boolean", DataType.BOOLEAN, true, 0);
        List<KeyValue> actual = UpdateService.readFromExcel(path, tableName, targetColumn);
        assertEquals(expected, actual);
    }

    @Test
    public void updateScriptTest() {
        RowCountCheck check = new RowCountCheck();
        String tableName = "address";
        String targetTableName = "address_16102016_05_39";
        String columnName = "address";

        boolean passed = UpdateService.checkForUpdate1(tableName, columnName, targetTableName, check);
        assertTrue(passed);
    }


    @Test
    public void checkUniqueRowsTest() {
        UniqueRowsCheck check = new UniqueRowsCheck();

        String tableName = "address";
        String targetTableName = "address_16102016_05_39";
        String columnName = "address";

        boolean passed = UpdateService.checkForUpdate2(tableName, columnName, targetTableName, check, 4);
        assertTrue(passed);
    }

    @Test
    public void checkRowsPresentTest(){
        PresentRowsCheck check = new PresentRowsCheck();

        String tableName = "address";
        String targetTableName = "address_16102016_05_39";
        String columnName = "address";

        boolean passed = UpdateService.checkForUpdate3(tableName, columnName, targetTableName, check, 4);
        assertTrue(passed);
    }

    /*@Test
    public  void updateTest() {

        String tableName = "address";
        String targetTableName = "address_16102016_05_39";
        String columnName = "regionid";
        String columnTargetName = "regionid";

        boolean passed = UpdateService.updateData(tableName, columnName, targetTableName, columnTargetName);
        assertTrue(passed);
    }*/

    @Test
    public void filterColumnsForUpdate() {
        /*Column{name='id', type='int', dataType=VARCHAR, isNullable=false, length=0}
        Column{name='address', type='varchar', dataType=VARCHAR, isNullable=false, length=50}
        Column{name='regionid', type='int', dataType=VARCHAR, isNullable=true, length=0}*/
        List<Column> data = new ArrayList<>();
        data.add(new Column("id", "int", DataType.VARCHAR, false, 0));
        data.add(new Column("address", "varchar", DataType.VARCHAR, false, 50));
        data.add(new Column("regionid", "int", DataType.FLOAT, true, 0));
        data.add(new Column("street", "varchar", DataType.VARCHAR, true, 140));


        List<Column> expected = new ArrayList<>();
        expected.add(new Column("id", "int", DataType.VARCHAR, false, 0));
        expected.add(new Column("address", "varchar", DataType.FLOAT, false, 50));

        String targetColumn = "address";
        List<Column> actual = UpdateService.filterForUpdate(data, targetColumn);
        assertEquals(expected, actual);
    }

    /* ADD DATA*/
    @Test
    public void filterStructureForAdd() {
        List<Column> data = new ArrayList<>();
        data.add(new Column("id", "int", DataType.VARCHAR, false, 0));
        data.add(new Column("address", "varchar", DataType.VARCHAR, false, 50));
        data.add(new Column("regionid", "int", DataType.FLOAT, true, 0));
        data.add(new Column("street", "varchar", DataType.VARCHAR, true, 140));


        List<Column> expected = new ArrayList<>();
        expected.add(new Column("address", "varchar", DataType.VARCHAR, false, 50));
        expected.add(new Column("regionid", "int", DataType.FLOAT, true, 0));
        expected.add(new Column("street", "varchar", DataType.VARCHAR, true, 140));

        List<Column> actual = UpdateService.filterForAdd(data);
        assertEquals(expected, actual);
    }

    @Test
    public void fillTempTableForAdd() throws SQLException {
        Column c1 = new Column("address", "varchar", DataType.VARCHAR, false, 50);
        Column c2 = new Column("regionid", "int", DataType.FLOAT, true, 0);

        List<Map<Column, KeyValue>> data = new ArrayList<>();
        data.add(map(pair(c1, new StringKeyValue("Nevskiy prospect, 1")), pair(c2, new FloatKeyValue(2f))));
        data.add(map(pair(c1, new StringKeyValue("Lenina st, 2")), pair(c2, new FloatKeyValue(1f))));
        data.add(map(pair(c1, new StringKeyValue("Moskovskaya, 17 ")), pair(c2, new FloatKeyValue(5f))));
        data.add(map(pair(c1, new StringKeyValue("Lenina st, 3")), pair(c2, new FloatKeyValue(1f))));


        String originalTableName = "address";
        List<Column> structure = UpdateService.getTableStructure(originalTableName);
        List<Column> targetColumns = UpdateService.filterForAdd(structure);
        String tempTableName = UpdateService.createTempTable(originalTableName, targetColumns);
        UpdateService.fillTable(tempTableName, data);
    }

    @Test
    public void readFileForAdd() {
        Column c1 = new Column("address", "varchar", DataType.VARCHAR, false, 50);
        Column c2 = new Column("regionid", "int", DataType.FLOAT, true, 0);

        List<Map<Column, KeyValue>> expected = new ArrayList<>();
        expected.add(map(pair(c1, new StringKeyValue("Nevskiy prospect, 1")), pair(c2, new FloatKeyValue(2f))));
        expected.add(map(pair(c1, new StringKeyValue("Lenina st, 2")), pair(c2, new FloatKeyValue(1f))));
        expected.add(map(pair(c1, new StringKeyValue("Moskovskaya, 17 ")), pair(c2, new FloatKeyValue(5f))));
        expected.add(map(pair(c1, new StringKeyValue("Lenina st, 3")), pair(c2, new FloatKeyValue(1f))));

        String tableName = "address";
        String path = "test/resources/address_add.xls";

        List<Column> structure = UpdateService.getTableStructure(tableName);
        List<Column> targetColumns = UpdateService.filterForAdd(structure);
        for (Column column : targetColumns) {
            System.out.println("target:" + column);
        }
        List<Map<Column, KeyValue>> actual = UpdateService.readForAdd(path, tableName, targetColumns);
        assertEquals(expected, actual);
    }

    @Test
    public void createSqlForInsertion() {
        List<Column> columns = new ArrayList<>();
        columns.add(new Column("address", "varchar", DataType.VARCHAR, false, 50));
        columns.add(new Column("regionid", "int", DataType.FLOAT, true, 0));

        String targetTable = "address";
        String temppTable = "address_tmp";

        String expected = "INSERT INTO address (address, regionid) SELECT t.address, t.regionid FROM address_tmp AS t";

        String actual = UpdateService.generateSqlForCopingData(targetTable, temppTable, columns);

        assertEquals(expected, actual);
    }

    private static Map<Column, KeyValue> map(Pair... pairs) {
        HashMap<Column, KeyValue> map = new HashMap<>();
        for (Pair pair : pairs) {
            map.put(pair.column, pair.value);
        }
        return map;
    }

    public static Pair pair(Column column, KeyValue s) {
        return new Pair(column, s);
    }


    public static Date date(int year, int month, int dayOfMonth) {
        return new GregorianCalendar(year, month - 1, dayOfMonth).getTime();
    }

    public static DateKeyValue dateKV(int year, int month, int dayOfMonth) {
        return new DateKeyValue(date(year, month, dayOfMonth));
    }


    static class Pair {
        Column column;
        KeyValue value;

        public Pair(Column column, KeyValue value) {
            this.column = column;
            this.value = value;
        }
    }
}
