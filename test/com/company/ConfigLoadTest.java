package com.company;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Александр on 03.05.2017.
 */
public class ConfigLoadTest {

    @Test
    public void checksGetForAddAddress() {
        AppConfig expected = new AppConfig("jdbc:sqlserver://ALEX-PC;instance=test_database;databaseName=test_database", "test", "test");
        AppConfig actual = ConnectionProvider.get().getAppConfig();

        assertEquals(expected, actual);
    }

    @Test
    public void saveAppConfig() throws IOException {
        AppConfig data = new AppConfig("jdbc:sqlserver://ALEX-PC;instance=test_database;databaseName=test_database", "test", "test");
        assertTrue(ConnectionProvider.get().save(data));
    }
}
