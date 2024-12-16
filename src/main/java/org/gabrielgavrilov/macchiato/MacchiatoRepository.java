package org.gabrielgavrilov.macchiato;

import org.gabrielgavrilov.macchiato.annotations.Column;
import org.gabrielgavrilov.macchiato.annotations.Id;
import org.gabrielgavrilov.macchiato.annotations.JoinTable;
import org.gabrielgavrilov.macchiato.annotations.Table;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class MacchiatoRepository<T> {

    private final Class<T> ENTITY = getGenericType();
    private final DataSource DATA_SOURCE = new DataSource();

    public MacchiatoRepository() {
    }

    public List<T> getAll() {
        List<T> entities = new ArrayList<>();
        String table = this.ENTITY.getAnnotation(Table.class).name();
        List<Field> fields = new ArrayList<>();

        for(Field field : this.ENTITY.getDeclaredFields()) {
            fields.add(field);
        }

        try {
            ResultSet rs = this.DATA_SOURCE.executeQuery(QueryBuilder.getAll(table));

            while(rs.next()) {
                T entity = (T) this.ENTITY.getDeclaredConstructor().newInstance();

                for(Field field : fields) {
                    if(field.isAnnotationPresent(Column.class)) {
                        field.setAccessible(true);
                        String columnName = field.getAnnotation(Column.class).name();
                        Object value = rs.getObject(columnName);
                        field.set(entity, value);
                    }
                    if(field.isAnnotationPresent(JoinTable.class)) {
                        JoinTable joinTableAnnotation = field.getAnnotation(JoinTable.class);
                        field.set(entity, joinTable(field.getType(), table, joinTableAnnotation.tableName(), joinTableAnnotation.columnName()));
                    }
                }

                entities.add(entity);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return entities;
    }

    public T findById(String id) {
        T entity = null;
        String table = this.ENTITY.getAnnotation(Table.class).name();
        String idField = null;

        for(Field field : this.ENTITY.getDeclaredFields()) {
            if(field.isAnnotationPresent(Id.class) && field.isAnnotationPresent(Column.class)) {
                idField = field.getAnnotation(Column.class).name();
            }
        }

        try {
            ResultSet rs = this.DATA_SOURCE.executeQuery(QueryBuilder.getById(table, idField, id));
            entity = (T) this.ENTITY.getDeclaredConstructor().newInstance();

            for(Field field : this.ENTITY.getDeclaredFields()) {
                if(field.isAnnotationPresent(Column.class)) {
                    field.setAccessible(true);
                    String columnName = field.getAnnotation(Column.class).name();
                    Object value = rs.getObject(columnName);
                    field.set(entity, value);
                }
                if(field.isAnnotationPresent(JoinTable.class)) {
                    JoinTable joinTableAnnotation = field.getAnnotation(JoinTable.class);
                    field.set(entity,joinTable(field.getType(), table, joinTableAnnotation.tableName(), joinTableAnnotation.columnName()));
                }
            }

        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return entity;
    }

    public void save(Object entity) {
        String table = this.ENTITY.getAnnotation(Table.class).name();
        List<String> fields = new ArrayList<>();
        List<String> values = new ArrayList<>();

        try {
            for(Field field : this.ENTITY.getDeclaredFields()) {
                fields.add(field.getAnnotation(Column.class).name());
                values.add(field.get(entity).toString());
            }

            this.DATA_SOURCE.executeQuery(QueryBuilder.save(table, fields, values));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    private Object joinTable(Class entity, String table, String joinTable, String joinColumn) {
        List<String> joinFields = new ArrayList<>();
        Object result = null;

        for(Field field : entity.getDeclaredFields()) {
            if(field.isAnnotationPresent(Column.class)) {
                joinFields.add(field.getAnnotation(Column.class).name());
            }
        }

        try {
            ResultSet rs = this.DATA_SOURCE.executeQuery(QueryBuilder.joinTable(table, joinTable, joinColumn, joinFields));

            while(rs.next()) {
                result = entity.getDeclaredConstructor().newInstance();

                for(Field field : result.getClass().getDeclaredFields()) {
                    field.setAccessible(true);
                    String columnName = field.getAnnotation(Column.class).name();
                    Object value = rs.getObject(columnName);
                    field.set(result, value);
                }
            }

        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private Class<T> getGenericType() {
        Class<?> clazz = this.getClass();
        Type type = clazz.getGenericSuperclass();
        ParameterizedType pt = (ParameterizedType) type;
        return (Class<T>)pt.getActualTypeArguments()[0];
    }

}
