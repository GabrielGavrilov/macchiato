package org.gabrielgavrilov.macchiato;

import org.gabrielgavrilov.macchiato.annotations.*;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
                entities.add(createPopulatedEntity(rs));
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
            String query = QueryBuilder.getById(this.ENTITY_TABLE_NAME, this.getEntityIdColumn(), id);
            ResultSet rs = this.DATA_SOURCE.executeQuery(query);
            entity = this.createPopulatedEntity(rs);
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
                    values.add(String.valueOf(field.get(entity)));
                }
            }
            this.DATA_SOURCE.executeQuery(QueryBuilder.save(this.ENTITY_TABLE_NAME, columns, values));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteById(String id) {
        try {
            this.DATA_SOURCE.executeQuery(QueryBuilder.delete(this.ENTITY_TABLE_NAME, this.getEntityIdColumn(), id));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void delete(Object entity) {
        try {
            this.deleteById(getIdValueFromObjectEntity(entity));
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
                foundEntity = this.createPopulatedEntityFromClass(entity, rs);
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

    private List<Field> getEntityFieldsFromClass(Class clazz) {
        List<Field> fields = new ArrayList<>();

        for(Field field : clazz.getDeclaredFields()) {
            fields.add(field);
        }

        return fields;
    }

    private String getEntityIdColumn() {
        for(Field field : this.ENTITY_DECLARED_FIELDS) {
            if(field.isAnnotationPresent(Id.class) && field.isAnnotationPresent(Column.class)) {
                return field.getAnnotation(Column.class).name();
            }
        }
        return null;
    }

    private String getIdValueFromObjectEntity(Object entity) throws Exception {
        for(Field field : entity.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class) && field.isAnnotationPresent(Column.class)) {
                field.setAccessible(true);
                return String.valueOf(field.get(entity));
            }
        }
        return null;
    }

    private T createPopulatedEntity(ResultSet rs) throws Exception {
        T entity = this.ENTITY.getDeclaredConstructor().newInstance();
        for(Field field : this.getEntityFields()) {
            this.populateEntityFieldWithJoinColumn(entity, field, rs);
        }
        return entity;
    }

    private Object createPopulatedEntityFromClass(Class clazz, ResultSet rs) throws Exception {
        Object entity = clazz.getDeclaredConstructor().newInstance();
        for(Field field : entity.getClass().getDeclaredFields()) {
            this.populateEntityField(entity, field, rs);
        }
        return entity;
    }

    private void populateEntityField(Object entity, Field field, ResultSet rs) throws Exception {
        if(field.isAnnotationPresent(Column.class)) {
            field.setAccessible(true);
            String entityColumnName = field.getAnnotation(Column.class).name();
            Object entityColumnValueInDatabase = rs.getObject(entityColumnName);
            field.set(entity, entityColumnValueInDatabase);
        }
    }

    private void populateEntityFieldWithJoinColumn(Object entity, Field field, ResultSet rs) throws Exception {
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
