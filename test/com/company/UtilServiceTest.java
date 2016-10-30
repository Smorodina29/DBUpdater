package com.company;

import com.company.data.Address;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

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
    public void getTableStructure() throws Exception{

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
        UpdateService.updateTable(tempTableName, data );
    }



}