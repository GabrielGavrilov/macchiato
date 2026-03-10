package org.gabrielgavrilov.macchiato;

import org.gabrielgavrilov.macchiato.annotations.*;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MacchiatoRepository<T> {

    private final Class<T> entityClass = (Class<T>) MacchiatoReflectionTools.getGenericClassType(this.getClass());
    private final String entityTableName = entityClass.getAnnotation(Table.class).name();
    private final DataSource dataSource;

    public MacchiatoRepository() {
        this.dataSource = Macchiato.getDataSource();
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
            String query = QueryBuilder.getAll(entityTableName);
            ResultSet rs = dataSource.executeQuery(query);
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
     *         If no entity was found, null type {@code T} will be returned.
     */
    public T findById(String id) {
        T entity = null;

        try {
            String query = QueryBuilder.getById(entityTableName, MacchiatoReflectionTools.getEntityIdColumn(entityClass), id);
            ResultSet rs = dataSource.executeQuery(query);
            if (rs != null) {
                rs.next();
                entity = this.createPopulatedEntity(rs);
            }
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
    public T save(Object entity) {
        T savedEntity = null;
        try {
            HashMap<String, String> columnsAndValues = MacchiatoReflectionTools.getColumnNamesAndValuesFromObject(entity);
            this.dataSource.execute(
                    QueryBuilder.save(
                            this.entityTableName,
                            new ArrayList<String>(columnsAndValues.keySet()),
                            new ArrayList<String>(columnsAndValues.values())
                    )
            );
            savedEntity = this.findById(MacchiatoReflectionTools.getIdValueFromObjectEntity(entity));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return savedEntity;
    }

    /**
     * Updates the given object entity that's already stored in the database. If entity does
     * not exist in the table, it will call the save() method instead.
     * <p>
     * This method constructs a query to update an existing record in the table associated
     * with the entity class and executes it. In case of any exceptions during the query execution,
     * the exception is caught and the stack trace is printed.
     * </p>
     *
     * @param entity a constructed object of the entity that's to be updated
     *               in the table associated with the entity class.
     */
    public T update(Object entity) {
        T updatedEntity = null;
        try {
            Object exists = this.findById(MacchiatoReflectionTools.getIdValueFromObjectEntity(entity));
            if (exists == null) {
                // TODO: throw something else
                throw new RuntimeException();
            }
            else {
                HashMap<String, String> columnsAndValues = MacchiatoReflectionTools.getColumnNamesAndValuesFromObject(entity);
                this.dataSource.execute(
                        QueryBuilder.update(
                                this.entityTableName,
                                new ArrayList<String>(columnsAndValues.keySet()),
                                new ArrayList<String>(columnsAndValues.values()),
                                MacchiatoReflectionTools.getEntityIdColumn(this.entityClass),
                                MacchiatoReflectionTools.getIdValueFromObjectEntity(entity)
                        )
                );
                updatedEntity = this.findById(MacchiatoReflectionTools.getIdValueFromObjectEntity(entity));
            }
        }
        catch(Exception e) {
            throw new RuntimeException();
        }
        return updatedEntity;
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
            Object exists = this.findById(id);
            if (exists != null) {
                this.dataSource.execute(QueryBuilder.delete(this.entityTableName, MacchiatoReflectionTools.getEntityIdColumn(this.entityClass), id));
            }
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
            this.deleteById(MacchiatoReflectionTools.getIdValueFromObjectEntity(entity));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Joins a singular relationship.
     * <p>
     * This method calls the join table query and returns a single foreign entity
     * related to the primary entity.
     * </p>
     * @param entity primary entity.
     * @param joinClass foreign entity class.
     * @param table primary table.
     * @param joinTable foreign table.
     * @param joinColumn foreign key.
     * @return singular foreign entity related to the primary entity.
     */
    private Object joinSingular(Object entity, Class joinClass, String table, String joinTable, String joinColumn) {
        Object foundEntity = null;

        try {
            ResultSet rs = this.dataSource.executeQuery(
                    QueryBuilder.joinTable(
                            table,
                            MacchiatoReflectionTools.getEntityIdColumn(this.entityClass),
                            MacchiatoReflectionTools.getIdValueFromObjectEntity(entity),
                            joinTable,
                            joinColumn,
                            MacchiatoReflectionTools.getColumnNamesFromClass(joinClass)
                    )
            );

            if (rs != null) {
                rs.next();
                foundEntity = this.createPopulatedEntityFromClass(joinClass, rs);
            }

        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return foundEntity;
    }

    /**
     * Joins multiple entities
     * <p>
     * This method calls the join table query and returns a list
     * of foreign entities related to the primary entity.
     * </p>
     * @param entity primary entity
     * @param joinClass foreign entity class
     * @param table primary table
     * @param joinTable foreign table
     * @param joinColumn foreign key
     * @return a list of foreign entities related to the primary entity.
     */
    private List<Object> joinMany(Object entity, Class joinClass, String table, String joinTable, String joinColumn) {
        List<Object> foundEntities = new ArrayList<>();

        try {
            ResultSet rs = this.dataSource.executeQuery(
                    QueryBuilder.joinTable(
                            table,
                            MacchiatoReflectionTools.getEntityIdColumn(this.entityClass),
                            MacchiatoReflectionTools.getIdValueFromObjectEntity(entity),
                            joinTable,
                            joinColumn,
                            MacchiatoReflectionTools.getColumnNamesFromClass(joinClass)
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

    /**
     * Populates and returns a {@code T} entity based on a given result set.
     * @param rs SQL result set
     * @return populated entity with the type {@code T}
     * @throws Exception
     */
    private T createPopulatedEntity(ResultSet rs) throws Exception {
        T entity = this.entityClass.getDeclaredConstructor().newInstance();
        for(Field field : MacchiatoReflectionTools.getEntityFields(this.entityClass)) {
            this.populateEntityFieldWithJoinColumn(entity, field, rs);
        }
        return entity;
    }

    /**
     * Populates and returns an object entity based on a given class.
     * @param clazz entity class
     * @param rs SQL result set
     * @return populated object entity
     * @throws Exception
     */
    private Object createPopulatedEntityFromClass(Class clazz, ResultSet rs) throws Exception {
        Object entity = clazz.getDeclaredConstructor().newInstance();
        for(Field field : entity.getClass().getDeclaredFields()) {
            this.populateEntityField(entity, field, rs);
        }
        return entity;
    }

    /**
     * Populates a given entity field based on a given result set.
     * @param entity object entity
     * @param field field to be populated
     * @param rs SQL result set
     * @throws Exception
     */
    private void populateEntityField(Object entity, Field field, ResultSet rs) throws Exception {
        if(field.isAnnotationPresent(Column.class)) {
            field.setAccessible(true);
            String entityColumnName = field.getAnnotation(Column.class).name();
            Object entityColumnValueInDatabase = rs.getObject(entityColumnName);
            field.set(entity, entityColumnValueInDatabase);
        }
    }

    /**
     * Populates a given entity field that supports join relationships, based on a given result set.
     * @param entity object entity
     * @param field field to be populated
     * @param rs SQL result set
     * @throws Exception
     */
    private void populateEntityFieldWithJoinColumn(Object entity, Field field, ResultSet rs) throws Exception {
        if(field.isAnnotationPresent(Column.class)) {
            field.setAccessible(true);
            String entityColumnName = field.getAnnotation(Column.class).name();
            Object entityColumnValueInDatabase = rs.getObject(entityColumnName);
            field.set(entity, entityColumnValueInDatabase);
        }
        if(field.isAnnotationPresent(JoinColumn.class) && field.isAnnotationPresent(OneToOne.class)) {
            JoinColumn joinColumnAnnotation = field.getAnnotation(JoinColumn.class);
            field.set(entity, this.joinSingular(entity, field.getType(), this.entityTableName, joinColumnAnnotation.table(), joinColumnAnnotation.column()));
        }
        if(field.isAnnotationPresent(JoinColumn.class) && (field.isAnnotationPresent(OneToMany.class) || field.isAnnotationPresent(ManyToOne.class))) {
            JoinColumn joinColumnAnnotation = field.getAnnotation(JoinColumn.class);
            field.set(entity, this.joinMany(entity, joinColumnAnnotation.referencedClass(), this.entityTableName, joinColumnAnnotation.table(), joinColumnAnnotation.column()));
        }
    }
}
