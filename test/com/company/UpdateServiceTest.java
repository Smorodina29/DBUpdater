package com.company;

import org.junit.Test;

import java.util.List;

/**
 * Created by Александр on 15.04.2017.
 */
public class UpdateServiceTest {


    @Test
    public void selectionAll() {
        List<String> allTables = UpdateService.getAllTablesNames();

        System.out.println("Tables:");
        for (String name : allTables) {
            System.out.println(name);
        }
        System.out.println("End of list");
    }
}
