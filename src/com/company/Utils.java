package com.company;

import org.apache.poi.ss.usermodel.Cell;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Александр on 17.07.2016.
 */
public class Utils {
    private Utils() {
    }

    public static Set<Pair<Integer, DataType>> allowedDataTypesPairs;

    static {
        allowedDataTypesPairs = new HashSet<>();
        allowedDataTypesPairs.add(new Pair<>(Cell.CELL_TYPE_STRING, DataType.VARCHAR));
        allowedDataTypesPairs.add(new Pair<>(Cell.CELL_TYPE_NUMERIC, DataType.FLOAT));
        allowedDataTypesPairs.add(new Pair<>(Cell.CELL_TYPE_NUMERIC, DataType.DATETIME));
        allowedDataTypesPairs.add(new Pair<>(Cell.CELL_TYPE_STRING, DataType.BOOLEAN));
        //blank
        allowedDataTypesPairs.add(new Pair<>(Cell.CELL_TYPE_BLANK, DataType.FLOAT));
        allowedDataTypesPairs.add(new Pair<>(Cell.CELL_TYPE_BLANK, DataType.VARCHAR));
        allowedDataTypesPairs.add(new Pair<>(Cell.CELL_TYPE_BLANK, DataType.DATETIME));
        allowedDataTypesPairs.add(new Pair<>(Cell.CELL_TYPE_BLANK, DataType.BOOLEAN));
    }


    public static void closeQuietly(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                //no-op
            }
        }
    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                //no-op
            }
        }
    }


    public static void closeQuietly(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                //ignore
            }
        }
    }
}
