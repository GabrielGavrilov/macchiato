package org.gabrielgavrilov.macchiato;

import org.gabrielgavrilov.macchiato.annotations.*;
import org.gabrielgavrilov.macchiato.exceptions.MacchiatoConnectionException;
import org.gabrielgavrilov.macchiato.exceptions.MacchiatoConstraintViolationException;
import org.gabrielgavrilov.macchiato.exceptions.MacchiatoEntityDoesNotExistException;
import org.gabrielgavrilov.macchiato.exceptions.MacchiatoException;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class MacchiatoQueryExecutor {

    private final MacchiatoDriverManager driverManager;

    /**
     * Initializes the DataSource and connects to the given database.
     */
    public MacchiatoQueryExecutor(MacchiatoDriverManager driverManager) {
        this.driverManager = driverManager;
    }

    /**
     * Executes a SQL query without returning a ResultSet.
     * @param query
     */
    public void execute(String query) {
        try {
            Connection connection = driverManager.getConnection();
            Statement stmt = connection.createStatement();
            stmt.execute(query);
            stmt.close();
            connection.close();
        }
        catch (SQLIntegrityConstraintViolationException e) {
            throw new MacchiatoConstraintViolationException(e.getMessage());
        }
        catch (SQLNonTransientConnectionException | SQLTransientConnectionException e) {
            throw new MacchiatoConnectionException(e.getMessage());
        }
        catch(SQLException e) {
            throw new MacchiatoException(e.getMessage());
        }
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
    public List<Object> executeFindAll(Class<?> entityClass, String entityTableName) {
        try {
            List<Object> entities = new ArrayList<>();
            Connection connection = driverManager.getConnection();
            Statement stmt = connection.createStatement();
            String query = QueryBuilder.getAll(entityTableName);
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next()) {
                entities.add(createPopulatedEntity(rs, entityClass, entityTableName));
            }
            rs.close();
            stmt.close();
            connection.close();
            return entities;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
    public Optional<Object> executeFindById(String id, Class<?> entityClass, String entityTableName) {
        try {
            Object entity = null;
            Connection connection = driverManager.getConnection();
            Statement stmt = connection.createStatement();
            String query = QueryBuilder.getById(entityTableName, MacchiatoReflectionTools.getColumnIdNameFromClass(entityClass), id);
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) {
                entity = this.createPopulatedEntity(rs, entityClass, entityTableName);
            } else {
                return Optional.empty();
            }
            rs.close();
            stmt.close();
            connection.close();
            return Optional.of(entity);
        }
        catch(Exception e) {
            throw new MacchiatoException(e.getMessage());
        }
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
    public Optional<Object> executeSave(Object entity, String entityTableName) {
        HashMap<String, String> columnsAndValues = MacchiatoReflectionTools.mapColumnNamesToValuesFromObject(entity);
        String query = QueryBuilder.save(
                entityTableName,
                new ArrayList<String>(columnsAndValues.keySet()),
                new ArrayList<String>(columnsAndValues.values())
        );

        this.execute(query);
        return executeFindById(MacchiatoReflectionTools.getColumnIdValueFromObject(entity), entity.getClass(), entityTableName);
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
    public Optional<Object> executeUpdate(Object entity, String entityTableName) {
        Optional<Object> exists = this.executeFindById(MacchiatoReflectionTools.getColumnIdValueFromObject(entity), entity.getClass(), entityTableName);
        if (exists.isEmpty()) {
            throw new MacchiatoEntityDoesNotExistException(String.format("No entity with id %s exists in %s", MacchiatoReflectionTools.getColumnIdValueFromObject(entity), entityTableName));
        }
        HashMap<String, String> columnsAndValues = MacchiatoReflectionTools.mapColumnNamesToValuesFromObject(entity);
        this.execute(
                QueryBuilder.update(
                        entityTableName,
                        new ArrayList<String>(columnsAndValues.keySet()),
                        new ArrayList<String>(columnsAndValues.values()),
                        MacchiatoReflectionTools.getColumnIdNameFromClass(entity.getClass()),
                        MacchiatoReflectionTools.getColumnIdValueFromObject(entity)
                )
        );
        return this.executeFindById(MacchiatoReflectionTools.getColumnIdValueFromObject(entity), entity.getClass(), entityTableName);
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
    public void executeDeleteById(String id, Class<?> entityClass, String entityTableName) {
        Optional<Object> exists = this.executeFindById(id, entityClass, entityTableName);
        if (exists.isPresent()) {
            this.execute(QueryBuilder.delete(entityTableName, MacchiatoReflectionTools.getColumnIdNameFromClass(entityClass), id));
        }
    }

    /**
     * Populates and returns a {@code T} entity based on a given result set.
     * @param rs SQL result set
     * @return populated entity with the type {@code T}
     * @throws Exception
     */
    private Object createPopulatedEntity(ResultSet rs, Class<?> entityClass, String entityTableName) throws Exception {
        Object entity = entityClass.getDeclaredConstructor().newInstance();
        for(Field field : MacchiatoReflectionTools.getListOfFieldsFromClass(entityClass)) {
            this.populateEntityFieldWithJoinColumn(entity, entityClass, entityTableName, field, rs);
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
    private Object joinSingular(Object entity, Class<?> entityClass,  Class joinClass, String table, String joinTable, String joinColumn) {
        Object foundEntity = null;

        try {
            Connection connection = driverManager.getConnection();
            Statement stmt = connection.createStatement();
            ResultSet rs =  stmt.executeQuery(QueryBuilder.joinTable(
                    table,
                    MacchiatoReflectionTools.getColumnIdNameFromClass(entityClass),
                    MacchiatoReflectionTools.getColumnIdValueFromObject(entity),
                    joinTable,
                    joinColumn,
                    MacchiatoReflectionTools.getAllColumnNamesFromClass(joinClass)
            ));

            if (rs != null) {
                rs.next();
                foundEntity = this.createPopulatedEntityFromClass(joinClass, rs);
            }

            rs.close();
            stmt.close();
            connection.close();

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
    private List<Object> joinMany(Object entity, Class<?> entityClass, Class joinClass, String table, String joinTable, String joinColumn) {
        List<Object> foundEntities = new ArrayList<>();

        try {
            Connection connection = this.driverManager.getConnection();
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(
                    QueryBuilder.joinTable(
                            table,
                            MacchiatoReflectionTools.getColumnIdNameFromClass(entityClass),
                            MacchiatoReflectionTools.getColumnIdValueFromObject(entity),
                            joinTable,
                            joinColumn,
                            MacchiatoReflectionTools.getAllColumnNamesFromClass(joinClass)
                    )
            );
            while(rs.next()) {
                foundEntities.add(this.createPopulatedEntityFromClass(joinClass, rs));
            }
            rs.close();
            stmt.close();
            connection.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return foundEntities;
    }

    /**
     * Populates a given entity field that supports join relationships, based on a given result set.
     * @param entity object entity
     * @param field field to be populated
     * @param rs SQL result set
     * @throws Exception
     */
    private void populateEntityFieldWithJoinColumn(Object entity, Class<?> entityClass, String entityTableName, Field field, ResultSet rs) throws Exception {
        if(field.isAnnotationPresent(Column.class)) {
            field.setAccessible(true);
            String entityColumnName = field.getAnnotation(Column.class).name();
            Object entityColumnValueInDatabase = rs.getObject(entityColumnName);
            field.set(entity, entityColumnValueInDatabase);
        }
        if(field.isAnnotationPresent(JoinColumn.class) && field.isAnnotationPresent(OneToOne.class)) {
            JoinColumn joinColumnAnnotation = field.getAnnotation(JoinColumn.class);
            field.set(entity, this.joinSingular(entity, entityClass, field.getType(), entityTableName, joinColumnAnnotation.table(), joinColumnAnnotation.column()));
        }
        if(field.isAnnotationPresent(JoinColumn.class) && (field.isAnnotationPresent(OneToMany.class) || field.isAnnotationPresent(ManyToOne.class))) {
            JoinColumn joinColumnAnnotation = field.getAnnotation(JoinColumn.class);
            field.set(entity, this.joinMany(entity, entityClass, joinColumnAnnotation.referencedClass(), entityTableName, joinColumnAnnotation.table(), joinColumnAnnotation.column()));
        }
    }
}
