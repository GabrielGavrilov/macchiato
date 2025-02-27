package org.gabrielgavrilov.macchiato;

import org.gabrielgavrilov.macchiato.annotations.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MacchiatoRepository<T> {

    private final Class<T> ENTITY = this.getGenericType();
    private final String ENTITY_TABLE_NAME = this.ENTITY.getAnnotation(Table.class).name();
    private final Field[] ENTITY_DECLARED_FIELDS = this.ENTITY.getDeclaredFields();

    private final DataSource DATA_SOURCE = new DataSource();

    public MacchiatoRepository() {
    }

    public List<T> getAll() {
        List<T> entities = new ArrayList<>();

        try {
            String query = QueryBuilder.getAll(this.ENTITY_TABLE_NAME);
            ResultSet rs = this.DATA_SOURCE.executeQuery(query);
            while(rs.next()) {
                entities.add(createPopulatedEntityBasedOnResultSet(rs));
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return entities;
    }

    public T findById(String id) {
        T entity = null;

        try {
            String query = QueryBuilder.getById(this.ENTITY_TABLE_NAME, this.getEntityIdField(), id);
            ResultSet rs = this.DATA_SOURCE.executeQuery(query);
            entity = this.createPopulatedEntityBasedOnResultSet(rs);
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return entity;
    }

    public void save(Object entity) {
        List<String> columns = new ArrayList<>();
        List<String> values = new ArrayList<>();

        try {
            for(Field field : this.getEntityFields()) {
                if(field.isAnnotationPresent(Column.class)) {
                    columns.add(field.getAnnotation(Column.class).name());
                    values.add(field.get(entity).toString());
                }
            }
            this.DATA_SOURCE.executeQuery(QueryBuilder.save(this.ENTITY_TABLE_NAME, columns, values));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteById(String id) {
        String table = this.ENTITY.getAnnotation(Table.class).name();
        String idField = null;

        for(Field field : this.ENTITY.getDeclaredFields()) {
            if(field.isAnnotationPresent(Id.class) && field.isAnnotationPresent(Column.class)) {
                idField = field.getAnnotation(Column.class).name();
            }
        }

        try {
            this.DATA_SOURCE.executeQuery(QueryBuilder.delete(table, idField, id));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void delete(String idColumn, String idValue) {
        String table = this.ENTITY.getAnnotation(Table.class).name();

        try {
            this.DATA_SOURCE.executeQuery(QueryBuilder.delete(table, idColumn, idValue));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    private Object joinColumn(Class entity, String table, String joinTable, String joinColumn) {
        List<String> joinFields = new ArrayList<>();
        Object foundEntity = null;

        for(Field field : entity.getDeclaredFields()) {
            if(field.isAnnotationPresent(Column.class)) {
                joinFields.add(field.getAnnotation(Column.class).name());
            }
        }

        try {
            ResultSet rs = this.DATA_SOURCE.executeQuery(QueryBuilder.joinTable(table, joinTable, joinColumn, joinFields));

            while(rs.next()) {
                foundEntity = entity.getDeclaredConstructor().newInstance();

                for(Field field : foundEntity.getClass().getDeclaredFields()) {
                    field.setAccessible(true);
                    String columnName = field.getAnnotation(Column.class).name();
                    Object value = rs.getObject(columnName);
                    field.set(foundEntity, value);
                }
            }

        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return foundEntity;
    }

    private List<Field> getEntityFields() {
        List<Field> fields = new ArrayList<>();

        for(Field field : this.ENTITY_DECLARED_FIELDS) {
            fields.add(field);
        }

        return fields;
    }

    private String getEntityIdField() {
        for(Field field : this.ENTITY_DECLARED_FIELDS) {
            if(field.isAnnotationPresent(Id.class) && field.isAnnotationPresent(Column.class)) {
                return field.getAnnotation(Column.class).name();
            }
        }
        return null;
    }

    private T createPopulatedEntityBasedOnResultSet(ResultSet rs) throws Exception {
        T entity = this.ENTITY.getDeclaredConstructor().newInstance();
        for(Field field : this.getEntityFields()) {
            this.populateEntityField(entity, field, rs);
        }
        return entity;
    }

    private void populateEntityField(T entity, Field field, ResultSet rs) throws Exception {
        if(field.isAnnotationPresent(Column.class)) {
            field.setAccessible(true);
            String entityColumnName = field.getAnnotation(Column.class).name();
            Object entityColumnValueInDatabase = rs.getObject(entityColumnName);
            field.set(entity, entityColumnValueInDatabase);
        }
        if(field.isAnnotationPresent(JoinColumn.class) && field.isAnnotationPresent(OneToOne.class)) {
            JoinColumn joinColumnAnnotation = field.getAnnotation(JoinColumn.class);
            field.set(entity, this.joinColumn(field.getType(), this.ENTITY_TABLE_NAME, joinColumnAnnotation.table(), joinColumnAnnotation.column()));
        }
    }

    private Class<T> getGenericType() {
        Class<?> clazz = this.getClass();
        Type type = clazz.getGenericSuperclass();
        ParameterizedType pt = (ParameterizedType) type;
        return (Class<T>)pt.getActualTypeArguments()[0];
    }
}
