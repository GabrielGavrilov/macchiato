package org.gabrielgavrilov.macchiato;

import org.gabrielgavrilov.macchiato.annotations.*;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
        catch(Exception e) {
            e.printStackTrace();
        }
    }

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

    public Object executeFindById(String id, Class<?> entityClass, String entityTableName) {
        try {
            Object entity = null;
            Connection connection = driverManager.getConnection();
            Statement stmt = connection.createStatement();
            String query = QueryBuilder.getById(entityTableName, MacchiatoReflectionTools.getColumnIdNameFromClass(entityClass), id);
            ResultSet rs = stmt.executeQuery(query);
            if (rs != null) {
                rs.next();
                entity = this.createPopulatedEntity(rs, entityClass, entityTableName);
            }
            rs.close();
            stmt.close();
            connection.close();
            return entity;
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Object executeSave(Object entity, String entityTableName) {
        HashMap<String, String> columnsAndValues = MacchiatoReflectionTools.mapColumnNamesToValuesFromObject(entity);
        String query = QueryBuilder.save(
                entityTableName,
                new ArrayList<String>(columnsAndValues.keySet()),
                new ArrayList<String>(columnsAndValues.values())
        );

        this.execute(query);
        return executeFindById(MacchiatoReflectionTools.getColumnIdValueFromObject(entity), entity.getClass(), entityTableName);
    }

    public Object executeUpdate(Object entity, String entityTableName) {
        Object exists = this.executeFindById(MacchiatoReflectionTools.getColumnIdValueFromObject(entity), entity.getClass(), entityTableName);
        if (exists == null) {
            return null;
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
