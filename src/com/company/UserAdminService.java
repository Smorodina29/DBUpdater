package com.company;

import com.company.ui.jfx.login.Role;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by Александр on 30.03.2017.
 */
public class UserAdminService {

    public static boolean isAuthenticated(String login, String password){
        /*Statement statement = null;
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
        }*/

        return "admin".equalsIgnoreCase(login) && "a".equals(password) || "user".equalsIgnoreCase(login) && "u".equals(password);
    }

    public static Role getRole(String name) {
        if ("admin".equalsIgnoreCase(name)) {
            return Role.ADMIN;
        } else {
            return Role.USER;
        }
    }
}
