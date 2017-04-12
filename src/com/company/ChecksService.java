package com.company;

import com.company.check.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Александр on 03.04.2017.
 */
public class ChecksService {

    public static List<Check> getChecksForAdd(String tableName) {
        return getChecksForUpdate(tableName, null);
    }

    public static List<Check> getChecksForUpdate(String tableName, String columnName) {
        List<Check> results = new ArrayList<>();
        Statement statement = null;

        if (tableName != null) {
            tableName = "'" + tableName + "'";
        }

        if (columnName != null) {
            columnName = "'" + columnName + "'";
        }
        try {
            statement = ConnectionProvider.get().getConnection().createStatement();
            String queryString = String.format("select q.id as id, q.query_text as query_text, q.name as name, q.message_text as message_text, q.check_type as check_type, q.validation_type as validation_type\n" +
                    " from query_check q join pair_checks p on p.query_check_id=q.id join for_update f on f.id=p.for_update_id where f.tablename=%s and f.columnname=%s;", tableName, columnName);

            System.out.println("query to get checks=\'" + queryString + "\'");
            ResultSet rs = statement.executeQuery(queryString);
            results = getChecksFrom(rs);
        } catch (SQLException e) {
            System.out.println("Error:" + e);
        } finally {
            Utils.closeQuietly(statement);
        }
        return results;
    }

    private static ArrayList<Check> getChecksFrom(ResultSet rs) throws SQLException {
        ArrayList<Check> checks = new ArrayList<>();
        while (rs.next()) {
            String id = rs.getString("id");
            String queryText = rs.getString("query_text");
            String name = rs.getString("name");
            String messageText = rs.getString("message_text");

            CheckType type = CheckType.valueOf(rs.getString("check_type"));
            ValidationMethod validationMethod = ValidationMethod.valueOf(rs.getString("validation_type"));

            checks.add(Check.create(id, queryText, name, messageText, type, validationMethod));
        }
        return checks;
    }

    public static List<Check> loadChecks() throws SQLException {
        List<Check> results = new ArrayList<>();
        Statement statement = null;
        try {
            statement = ConnectionProvider.get().getConnection().createStatement();
            String queryString = "select id, name, check_type, validation_type, query_text, message_text from query_check;";
            System.out.println("query to load checks=\'" + queryString + "\'");
            ResultSet rs = statement.executeQuery(queryString);
            results = getChecksFrom(rs);
        } finally {
            Utils.closeQuietly(statement);
        }
        return results;
    }

    public static void update(List<Check> updated) throws SQLException {
        if (updated == null || updated.isEmpty()) return;
        String quryString = "update query_check set query_text=?, name=?, check_type=?, validation_type=?, message_text=? where id=?;";
        PreparedStatement ps = null;
        try {
            ps = ConnectionProvider.get().getConnection().prepareStatement(quryString);
            for (Check check : updated) {
                ps.setString(1, check.getQueryText());
                ps.setString(2, check.getName());
                ps.setString(3, check.getType().name());
                ps.setString(4, check.getValidationMethod().name());
                ps.setString(5, check.getMessageText());
                ps.setInt(6, Integer.parseInt(check.getId()));
                ps.executeUpdate();
            }
        } finally {
            Utils.closeQuietly(ps);
        }
    }

    public static void delete(List<Check> updated) throws SQLException {
        if (updated == null || updated.isEmpty()) return;
        String quryString = "delete from query_check where id=?;";
        PreparedStatement ps = null;
        try {
            ps = ConnectionProvider.get().getConnection().prepareStatement(quryString);
            for (Check check : updated) {
                ps.setInt(1, Integer.parseInt(check.getId()));
                ps.executeUpdate();
            }
        } finally {
            Utils.closeQuietly(ps);
        }
    }
}
