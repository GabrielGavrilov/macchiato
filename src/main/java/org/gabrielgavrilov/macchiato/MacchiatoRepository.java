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

    /**
     * Retrieves all entities from the database and returns them as a list.
     * <p>
     * This method constructs a query to fetch all records from the table associated
     * with the entity class, executes the query, and populates a list of entities
     * from the result set. In case of any exceptions during the query execution or
     * entity population, the exception is caught and its stack trace is printed.
     * </p>
     *
     * @return A list of all entities of type {@code T} retrieved from the database.
     *         If no entities are found, an empty list will be returned.
     */
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

    /**
     * Retrieves an entity from the database with the given id and returns it.
     * <p>
     * This method constructs a query to fetch a singular record from the table associated
     * with the entity class, executes the query, and populates a new entity from the result set.
     * In case of any exceptions during the query execution or entity population, the exception is
     * caught and its stack trace is printed.
     * </p>
     *
     * @param id a String version of the id.
     * @return An entity with the type {@code T} retrieved from the database.
     */
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

    /**
     * Saves the given object entity into the database.
     * <p>
     * This method constructs a query to insert a new record into the table associated
     * with the entity class and executes it. In case of any exceptions during the query execution,
     * the exception is caught and its stack trace is printed.
     * </p>
     *
     * @param entity a constructed object of the entity that's to be saved
     *               in the table associated with the entity class.
     */
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

    /**
     * Updates the given object entity that's already stored in the database.
     * <p>
     * This method constructs a query to update an existing record in the table associated
     * with the entity class and executes it. In case of any exceptions during the query execution,
     * the exception is caught and the stack trace is printed.
     * </p>
     *
     * @param entity a constructed object of the entity that's to be updated
     *               in the table associated with the entity class.
     */
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

    /**
     * Finds a stored entity in the table with the given id and deletes it.
     * <p>
     * This method constructs a query to delete an existing entity with the given id
     * in the table associated with the entity class, and executes it. In case of any exceptions
     * during the query execution, the exception is caught and the stack trace is printed.
     * </p>
     * @param id a String version of the id.
     */
    public void deleteById(String id) {
        try {
            this.DATA_SOURCE.execute(QueryBuilder.delete(this.ENTITY_TABLE_NAME, this.getEntityIdColumn(), id));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Finds a stored entity in the table with the given constructed object of the entity and deletes it.
     * <p>
     * This method gets the id value from the given constructed object of the entity
     * and calls the deleteById() method.
     * </p>
     * @param entity constructed object of the entity that is to be deleted
     *               from the table associated with the entity class.
     */
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
