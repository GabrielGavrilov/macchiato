package org.gabrielgavrilov.macchiato;

import org.gabrielgavrilov.macchiato.annotations.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MacchiatoRepository<T> {

    private final Class<T> entityClass = (Class<T>) MacchiatoReflectionTools.getGenericClassType(this.getClass());
    private final String entityTableName = entityClass.getAnnotation(Table.class).name();
    private final MacchiatoQueryExecutor queryExecutor;

    public MacchiatoRepository() {
        this.queryExecutor = new MacchiatoQueryExecutor(Macchiato.getDriverManager());
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
            entities = (List<T>) queryExecutor.executeFindAll(entityClass, entityTableName);

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
            entity = (T) queryExecutor.executeFindById(id, this.entityClass, this.entityTableName);
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
            HashMap<String, String> columnsAndValues = MacchiatoReflectionTools.mapColumnNamesToValuesFromObject(entity);
            this.queryExecutor.execute(
                    QueryBuilder.save(
                            this.entityTableName,
                            new ArrayList<String>(columnsAndValues.keySet()),
                            new ArrayList<String>(columnsAndValues.values())
                    )
            );
            savedEntity = this.findById(MacchiatoReflectionTools.getColumnIdValueFromObject(entity));
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
            Object exists = this.findById(MacchiatoReflectionTools.getColumnIdValueFromObject(entity));
            if (exists == null) {
                // TODO: throw something else
                throw new RuntimeException();
            }
            else {
                HashMap<String, String> columnsAndValues = MacchiatoReflectionTools.mapColumnNamesToValuesFromObject(entity);
                this.queryExecutor.execute(
                        QueryBuilder.update(
                                this.entityTableName,
                                new ArrayList<String>(columnsAndValues.keySet()),
                                new ArrayList<String>(columnsAndValues.values()),
                                MacchiatoReflectionTools.getColumnIdNameFromClass(this.entityClass),
                                MacchiatoReflectionTools.getColumnIdValueFromObject(entity)
                        )
                );
                updatedEntity = this.findById(MacchiatoReflectionTools.getColumnIdValueFromObject(entity));
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
                this.queryExecutor.execute(QueryBuilder.delete(this.entityTableName, MacchiatoReflectionTools.getColumnIdNameFromClass(this.entityClass), id));
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
            this.deleteById(MacchiatoReflectionTools.getColumnIdValueFromObject(entity));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }



}
