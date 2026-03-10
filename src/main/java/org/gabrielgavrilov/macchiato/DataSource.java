package org.gabrielgavrilov.macchiato;

import org.gabrielgavrilov.macchiato.annotations.*;

import java.lang.reflect.Field;
import java.sql.*;

public class DataSource {

    private final String databaseUrl;

    /**
     * Initializes the DataSource and connects to the given database.
     */
    public DataSource(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    public Connection getConnection() {
        try {
            return DriverManager.getConnection(this.databaseUrl);
        } catch (SQLException e) {
            // Todo: fix this
            throw new RuntimeException(e);
        }
    }

    /**
     * Executes a SQL query and returns a ResultSet.
     * @param query
     * @return ResultSet
     */
    public ResultSet executeQuery(String query) {
        try {
            Statement stmt = this.getConnection().createStatement();
            return stmt.executeQuery(query);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Object executeFindById(String query, Class<?> entityClass) {
        try {
            Object entity = null;
            Statement stmt = this.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if (rs != null) {
                rs.next();
                entity = this.createPopulatedEntity(rs, entityClass);
            }
            return entity;
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Executes a SQL query without returning a ResultSet.
     * @param query
     */
    public void execute(String query) {
        try {
            Statement stmt = this.getConnection().createStatement();
            stmt.execute(query);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Populates and returns a {@code T} entity based on a given result set.
     * @param rs SQL result set
     * @return populated entity with the type {@code T}
     * @throws Exception
     */
    private Object createPopulatedEntity(ResultSet rs, Class<?> clazz) throws Exception {
        Object entity = clazz.getDeclaredConstructor().newInstance();
        for(Field field : MacchiatoReflectionTools.getEntityFields(clazz)) {
            this.populateEntityFieldWithJoinColumn(entity, field, rs);
        }
        return entity;
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
//        if(field.isAnnotationPresent(JoinColumn.class) && field.isAnnotationPresent(OneToOne.class)) {
//            JoinColumn joinColumnAnnotation = field.getAnnotation(JoinColumn.class);
//            field.set(entity, this.joinSingular(entity, field.getType(), this.entityTableName, joinColumnAnnotation.table(), joinColumnAnnotation.column()));
//        }
//        if(field.isAnnotationPresent(JoinColumn.class) && (field.isAnnotationPresent(OneToMany.class) || field.isAnnotationPresent(ManyToOne.class))) {
//            JoinColumn joinColumnAnnotation = field.getAnnotation(JoinColumn.class);
//            field.set(entity, this.joinMany(entity, joinColumnAnnotation.referencedClass(), this.entityTableName, joinColumnAnnotation.table(), joinColumnAnnotation.column()));
//        }
    }
}
