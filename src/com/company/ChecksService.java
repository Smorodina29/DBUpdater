package com.company;

import com.company.check.*;

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
            String queryString = String.format("select q.query_text as query_text, q.name as name, q.message_text as message_text, q.check_type as check_type, q.validation_type as validation_type\n" +
                    " from query_check q join pair_checks p on p.query_check_id=q.id join for_update f on f.id=p.for_update_id where f.tablename=%s and f.columnname=%s;", tableName, columnName);

            System.out.println("query to get checks=\'" + queryString + "\'");
            ResultSet rs = statement.executeQuery(queryString);
            while (rs.next()) {
                String queryText = rs.getString("query_text");
                String name = rs.getString("name");
                String messageText = rs.getString("message_text");

                CheckType type = CheckType.valueOf(rs.getString("check_type"));
                ValidationMethod validationMethod = ValidationMethod.valueOf(rs.getString("validation_type"));

                Check check;

                if (ValidationMethod.ALL.equals(validationMethod)) {
                    check = new AllValidationCheck(queryText, name, messageText, type);
                } else if (ValidationMethod.ZERO.equals(validationMethod)) {
                    check = new ZeroValidationCheck(queryText, name, messageText, type);
                } else {
                    throw new RuntimeException("Unknown validation method `" + validationMethod + "\'");
                }
                results.add(check);
            }
        } catch (SQLException e) {
            System.out.println("Error:" + e);
        } finally {
            Utils.closeQuietly(statement);
        }
        return results;
    }
}
