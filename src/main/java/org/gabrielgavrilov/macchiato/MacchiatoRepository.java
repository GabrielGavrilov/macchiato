package org.gabrielgavrilov.macchiato;

import org.gabrielgavrilov.macchiato.annotations.Column;
import org.gabrielgavrilov.macchiato.annotations.Entity;
import org.gabrielgavrilov.macchiato.annotations.Table;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MacchiatoRepository<T> {

    private Controller _CONTROLLER = new Controller("jdbc:sqlite:test.db");
    private Class<T> _ENTITY;

    public MacchiatoRepository() {
        this._ENTITY = getGenericType();
//        if(clazz.isAnnotationPresent(Table.class)) {
//            Table table = clazz.getAnnotation(Table.class);
//
//            for(Field field : clazz.getDeclaredFields()) {
//                if(field.isAnnotationPresent(Column.class)) {
//                    Column column = field.getAnnotation(Column.class);
//                    System.out.println(column.name());
//                }
//            }
//
//        }
    }


    public List<T> getAll() {
        List<T> entities = new ArrayList<>();
        List<Field> fields = new ArrayList<>();
        Table table = this._ENTITY.getAnnotation(Table.class);

        for(Field field : this._ENTITY .getDeclaredFields()) {
            fields.add(field);
        }

        try(
                Connection conn = DriverManager.getConnection("jdbc:sqlite:test.db");
                Statement statement = conn.createStatement();
        ) {

            ResultSet rs = statement.executeQuery(String.format("SELECT * FROM %s", table.name()));

            while(rs.next()) {
                T entity = (T) this._ENTITY.getDeclaredConstructor().newInstance();

                for(int i = 0; i < fields.size(); i++) {
                    Field field = fields.get(i);
                    field.setAccessible(true);
                    String columnName = field.getAnnotation(Column.class).name();

                    Object value = rs.getObject(columnName);

                    field.set(entity, value);
                }

                entities.add(entity);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return entities;
    }

    public void save(Object entity) {
        List<Field> fields = new ArrayList<>();
        Table table = this._ENTITY.getAnnotation(Table.class);
        String query = "INSERT INTO " + table.name() + "(";

        for(Field field : this._ENTITY.getDeclaredFields()) {
            String columnName = field.getAnnotation(Column.class).name();
            query += columnName + ", ";
            fields.add(field);
        }

        query = query.substring(0, query.length() - 2);
        query += ") VALUES(";



        try(
                Connection conn = DriverManager.getConnection("jdbc:sqlite:test.db");
                Statement statement = conn.createStatement();
        ) {



            for(Field field : fields) {
                String data = (String)field.get(entity);
                query += String.format("'%s', ", data);
            }
            query = query.substring(0, query.length() - 2);
            query += ")";

            statement.executeUpdate(query);
            ResultSet rs = statement.executeQuery("SELECT * FROM " + table.name());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }

    private Class<T> getGenericType() {
        Class<?> clazz = this.getClass();
        Type type = clazz.getGenericSuperclass();
        ParameterizedType pt = (ParameterizedType) type;
        return (Class<T>)pt.getActualTypeArguments()[0];
    }

}
