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

    private Class<T> _ENTITY;

    public MacchiatoRepository() {
        this._ENTITY = getGenericType();
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
        List<String> fields = new ArrayList<>();
        List<String> values = new ArrayList<>();
        Table table = this._ENTITY.getAnnotation(Table.class);

        try {
            for(Field field : this._ENTITY.getDeclaredFields()) {
                String columnName = field.getAnnotation(Column.class).name();
                fields.add(columnName);
                values.add((String)field.get(entity));
            }

            System.out.println(QueryBuilder.save(table.name(), fields, values));
        }
        catch(Exception e) {
            e.printStackTrace();
        }

    }

    private Class<T> getGenericType() {
        Class<?> clazz = this.getClass();
        Type type = clazz.getGenericSuperclass();
        ParameterizedType pt = (ParameterizedType) type;
        return (Class<T>)pt.getActualTypeArguments()[0];
    }

}
