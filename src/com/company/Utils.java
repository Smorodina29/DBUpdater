package com.company;

import org.apache.poi.ss.usermodel.Cell;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * Created by Александр on 17.07.2016.
 */
public class Utils {
    private Utils() {
    }

    public static Set<Pair<Integer, DataType>> allowedDataTypesPairs;
    public static Map<String, DataType> str2DateType;

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


        str2DateType = new HashMap<>();
        str2DateType.put("varchar", DataType.VARCHAR);
        str2DateType.put("int", DataType.FLOAT);
        str2DateType.put("datetime", DataType.DATETIME);
        str2DateType.put("boolean", DataType.BOOLEAN);
    }

    public static DataType getType(String dataType) {
        return str2DateType.get(dataType);
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

    public static String mkString(Collection<Column> mustPresent) {
        StringBuilder sb = new StringBuilder();

        Iterator<Column> iterator = mustPresent.iterator();
        boolean first = true;
        while (iterator.hasNext()) {
            Column next = iterator.next();
            if (!first) {
                sb.append(", ");
            } else {
                first = false;
            }
            sb.append(next.name);
        }
        return sb.toString();
    }
}
