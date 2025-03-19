package org.gabrielgavrilov.macchiato;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class QueryBuilder {

    /**
     * Builds a get all query.
     * @param tableName table
     * @return SQL query
     */
    public static String getAll(String tableName) {
        return String.format("SELECT * FROM %s;", tableName);
    }

    /**
     * builds a get by id query.
     * @param tableName table
     * @param idColumn id column
     * @param idValue id value
     * @return SQL query
     */
    public static String getById(String tableName, String idColumn, String idValue) {
        return String.format(
                "SELECT * FROM %s WHERE %s = %s;",
                tableName,
                idColumn,
                idValue
        );
    }

    /**
     * Builds a save query.
     * @param tableName table
     * @param columns list of table columns
     * @param values list of column values
     * @return SQL query
     */
    public static String save(String tableName, List<String> columns, List<String> values) {
        return String.format(
                "INSERT INTO %s (%s) VALUES (%s);",
                tableName,
                String.join(", ", columns),
                values.stream()
                        .map(value -> String.format("'%s'", value))
                        .collect(Collectors.joining(", "))
        );
    }

    /**
     * Builds an update query.
     * @param tableName table
     * @param columns list of table columns
     * @param values list of column values
     * @param idColumn id column
     * @param idValue id value
     * @return SQL query
     */
    public static String update(String tableName, List<String> columns, List<String> values, String idColumn, String idValue) {
        List<String> set = new ArrayList<>();
        for(int i = 0; i < columns.size(); i++) {
            set.add(String.format("%s = '%s'", columns.get(i), values.get(i)));
        }
        return String.format(
                "UPDATE %s SET %s WHERE %s = %s;",
                tableName,
                String.join(", ", set),
                idColumn,
                idValue
        );
    }

    /**
     * Builds a delete query.
     * @param tableName table
     * @param idColumn
     * @param idValue
     * @return SQL query
     */
    public static String delete(String tableName, String idColumn, String idValue) {
        return String.format(
                "DELETE FROM %s WHERE %s = %s;",
                tableName,
                idColumn,
                idValue
        );
    }

    /**
     * Builds a join table query.
     * @param tableName primary table
     * @param idColumn primary id column
     * @param idValue primary id value
     * @param joinTable foreign table
     * @param joinColumn foreign join column
     * @param joinFields list of foreign join fields.
     * @return SQL query
     */
    public static String joinTable(String tableName, String idColumn, String idValue, String joinTable, String joinColumn, List<String> joinFields) {
        List<String> transformedFields = joinFields
                .stream()
                .map(field -> String.format("%s.%s",joinTable,field))
                .collect(Collectors.toList());
        return String.format(
                "SELECT %s\nFROM %s\nJOIN %s\nON (%s.%s = %s.%s) WHERE %s.%s = %s;",
                String.join(", ", transformedFields),
                tableName,
                joinTable,
                tableName,
                joinColumn,
                joinTable,
                joinColumn,
                tableName,
                idColumn,
                idValue
        );
    }

}
