package org.gabrielgavrilov.macchiato;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

public class QueryBuilder {

    public static String getAll(String tableName) {
        return String.format("SELECT * FROM %s;", tableName);
    }

    public static String save(String tableName, List<String> fields, List<String> values) {
        return String.format(
                "INSERT INTO %s (%s) VALUES (%s);",
                tableName,
                String.join(", ", fields),
                values.stream()
                        .map(value -> String.format("'%s'", value))
                        .collect(Collectors.joining(", "))
        );
    }

    public static String getById(String tableName, String idField, String idValue) {
        return String.format(
                "SELECT * FROM %s WHERE %s = %s;",
                tableName,
                idField,
                idValue
        );
    }

}
