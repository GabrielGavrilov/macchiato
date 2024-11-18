package org.gabrielgavrilov.macchiato;

import org.gabrielgavrilov.macchiato.annotations.Column;
import org.gabrielgavrilov.macchiato.annotations.Entity;
import org.gabrielgavrilov.macchiato.annotations.Table;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

public class Macchiato {

    public Macchiato() {

    }

    public void checkIfSerializable(Object object) {
        if(Objects.isNull(object)) {
            System.out.println("Object to serialize is null");
            return;
        }

        Class<?> clazz = object.getClass();
        if(!clazz.isAnnotationPresent(Entity.class)) {
            System.out.println(String.format("'%s' is not an entity.", clazz.getSimpleName()));
            return;
        }
    }

    public void getEntityInformation(Object object) {

        Class<?> clazz = object.getClass();
        Map<String, String> entityInformation = new HashMap<>();
        clazz.isAnnotationPresent(Table.class);

        Table table = clazz.getAnnotation(Table.class);
        System.out.println(table.name());

        for(Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            if(field.isAnnotationPresent(Column.class)) {
                try {
                    Column column = field.getAnnotation(Column.class);
                    System.out.println(String.format("%s -> %s -> %s", column.name(), field.getName(), field.get(object)));
                    entityInformation.put(field.getName(), (String)field.get(object));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }

    public void findAll(Object object) {
        Class<?> clazz = object.getClass();
        List<Object> find = new ArrayList<>();
        String tableName = clazz.getAnnotation(Table.class).name();

        String query = selectQueryBuilder("*", tableName);

        try (
                Connection connection = DriverManager.getConnection("jdbc:sqlite:test.db");
                Statement statement = connection.createStatement();
        )
        {
            statement.setQueryTimeout(30);
            ResultSet rs = statement.executeQuery("SELECT * FROM users");

            while(rs.next()) {
                System.out.println(rs.getString("first_name"));
            }

        }
        catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public String selectQueryBuilder(String fields, String table) {
        return String.format("SELECT %s FROM %s;", fields, table);
    }

}
