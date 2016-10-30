package com.company;

import java.sql.*;

/**
 * Created by Александр on 10.07.2016.
 */
public class MSSQLServer {

    public void dbConnect(String db_connect_string, String db_userid, String db_password) {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            Connection conn = DriverManager.getConnection(db_connect_string, db_userid, db_password);
            System.out.println("Connected.");
            Statement statement = conn.createStatement();
//            String queryString = "Use [test_database] SELECT Name FROM dbo.sysobjects  WHERE (xtype = 'U')";
            String queryString = "select tablename from for_update";
            ResultSet rs = statement.executeQuery(queryString);
            while (rs.next()) {
                System.out.println(rs.getString(1));
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Class Not Found Exception:" + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("SQL Exception:" + e.getMessage());
            e.printStackTrace();
        }
    }
}
