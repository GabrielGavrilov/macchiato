package org.gabrielgavrilov.macchiato;

import org.gabrielgavrilov.macchiato.annotations.*;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
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
        try {
            HashMap<String, String> columnsAndValues = this.getColumnNamesAndValuesFromObject(entity);
            this.DATA_SOURCE.execute(
                    QueryBuilder.save(
                            this.ENTITY_TABLE_NAME,
                            new ArrayList<String>(columnsAndValues.keySet()),
                            new ArrayList<String>(columnsAndValues.values())
                    )
            );
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void update(Object entity) {
        try {
            HashMap<String, String> columnsAndValues = this.getColumnNamesAndValuesFromObject(entity);
            this.DATA_SOURCE.execute(
                    QueryBuilder.update(
                            this.ENTITY_TABLE_NAME,
                            new ArrayList<String>(columnsAndValues.keySet()),
                            new ArrayList<String>(columnsAndValues.values()),
                            this.getEntityIdColumn(),
                            this.getIdValueFromObjectEntity(entity)
                    )
            );
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteById(String id) {
        try {
            this.DATA_SOURCE.execute(QueryBuilder.delete(this.ENTITY_TABLE_NAME, this.getEntityIdColumn(), id));
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

    private Object joinSingular(Object entity, Class joinClass, String table, String joinTable, String joinColumn) {
        Object foundEntity = null;

        try {
            ResultSet rs = this.DATA_SOURCE.executeQuery(
                    QueryBuilder.joinTable(
                            table,
                            this.getEntityIdColumn(),
                            this.getIdValueFromObjectEntity(entity),
                            joinTable,
                            joinColumn,
                            this.getColumnNamesFromClass(joinClass)
                    )
            );
            foundEntity = this.createPopulatedEntityFromClass(joinClass, rs);

        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return foundEntity;
    }

    private List<Object> joinMany(Object entity, Class joinClass, String table, String joinTable, String joinColumn) {
        List<Object> foundEntities = new ArrayList<>();

        try {
            ResultSet rs = this.DATA_SOURCE.executeQuery(
                    QueryBuilder.joinTable(
                            table,
                            this.getEntityIdColumn(),
                            this.getIdValueFromObjectEntity(entity),
                            joinTable,
                            joinColumn,
                            this.getColumnNamesFromClass(joinClass)
                    )
            );
            while(rs.next()) {
                foundEntities.add(this.createPopulatedEntityFromClass(joinClass, rs));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return foundEntities;
    }

    private List<Field> getEntityFields() {
        List<Field> fields = new ArrayList<>();

        for(Field field : this.ENTITY_DECLARED_FIELDS) {
            fields.add(field);
        }

        return fields;
    }

    private List<String> getColumnNamesFromClass(Class clazz) {
        List<String> columns = new ArrayList<>();
        for(Field field : clazz.getDeclaredFields()) {
            if(field.isAnnotationPresent(Column.class)) {
                columns.add(field.getAnnotation(Column.class).name());
            }
        }

        return columns;
    }

    private HashMap<String, String> getColumnNamesAndValuesFromObject(Object entity) throws Exception {
        HashMap<String, String> columnsAndValues = new HashMap<>();

        for (Field field : entity.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class)) {
                columnsAndValues.put(field.getAnnotation(Column.class).name(), String.valueOf(field.get(entity)));
            }
        }
        return columnsAndValues;
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
            field.set(entity, this.joinSingular(entity, field.getType(), this.ENTITY_TABLE_NAME, joinColumnAnnotation.table(), joinColumnAnnotation.column()));
        }
        if(field.isAnnotationPresent(JoinColumn.class) && (field.isAnnotationPresent(OneToMany.class) || field.isAnnotationPresent(ManyToOne.class))) {
            JoinColumn joinColumnAnnotation = field.getAnnotation(JoinColumn.class);
            field.set(entity, this.joinMany(entity, joinColumnAnnotation.referencedClass(), this.ENTITY_TABLE_NAME, joinColumnAnnotation.table(), joinColumnAnnotation.column()));
        }
    }

    private Class<T> getGenericType() {
        Class<?> clazz = this.getClass();
        Type type = clazz.getGenericSuperclass();
        ParameterizedType pt = (ParameterizedType) type;
        return (Class<T>)pt.getActualTypeArguments()[0];
    }
}
