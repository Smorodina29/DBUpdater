package com.company;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by Александр on 17.07.2016.
 */
public class ConnectionProvider {

    private static ConnectionProvider provider = new ConnectionProvider();
    private String db_userid = "test";
    private String db_password = "test";
    private String db_connect_string = "jdbc:sqlserver://ALEX-PC;instance=test_database;databaseName=test_database";

    private ConnectionProvider() {
    }

    public Connection getConnection() {
        Connection connection = null;
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            connection = DriverManager.getConnection(db_connect_string, db_userid, db_password);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return connection;
    }

    public static ConnectionProvider get() {
        return provider;
    }
}
