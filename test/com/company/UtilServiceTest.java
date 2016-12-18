package com.company;

import com.company.check.PresentRowsCheck;
import com.company.check.RowCountCheck;
import com.company.check.UniqueRowsCheck;
import com.company.data.*;
import org.junit.Test;

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
        List<String> actual = UpdateService.getTableColumns(tableName);
        assertEquals(actual, expected);
    }

    @Test
    public void getTableStructure() throws Exception {

    }

    @Test
    public void exportTableToFile() {
        String tableName = "address";
        String path = "C:\\tmp\\1.xls";
        UpdateService.exportTableToFile(tableName, path);
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
        expected.add(new FloatKeyValue(1, 2d));
        expected.add(new FloatKeyValue(2, 1d));
        expected.add(new FloatKeyValue(3, 5d));
        expected.add(new FloatKeyValue(2, 1d));

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
        expected.add(new DateKeyValue(1, new GregorianCalendar(2016, 10, 15).getTime()));
        expected.add(new DateKeyValue(2, new GregorianCalendar(2019, 0, 25).getTime()));
        expected.add(new DateKeyValue(3, new GregorianCalendar(2016, 4, 9).getTime()));
        expected.add(new DateKeyValue(2, new GregorianCalendar(2016, 11, 12).getTime()));

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
}