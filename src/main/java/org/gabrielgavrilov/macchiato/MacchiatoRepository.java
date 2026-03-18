package org.gabrielgavrilov.macchiato;

import org.gabrielgavrilov.macchiato.annotations.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MacchiatoRepository<T> {

    private final Class<T> entityClass = (Class<T>) MacchiatoReflectionTools.getGenericClassType(this.getClass());
    private final String entityTableName = entityClass.getAnnotation(Table.class).name();
    private final MacchiatoQueryExecutor queryExecutor;

    public MacchiatoRepository() {
        queryExecutor = new MacchiatoQueryExecutor(Macchiato.getDriverManager());
    }

    public List<T> findAll() {
        return queryExecutor
                .executeFindAll(entityClass, entityTableName)
                .stream()
                .map(e -> (T) e)
                .collect(Collectors.toList());
    }

    public Optional<T> findById(String id) {
        return queryExecutor
                .executeFindById(id, entityClass, entityTableName)
                .map(e -> (T) e);
    }

    public Optional<T> save(T entity) {
        return queryExecutor
                .executeSave(entity, entityTableName)
                .map(e -> (T) e);
    }

    public Optional<T> update(Object entity) {
        return queryExecutor
                .executeUpdate(entity, entityTableName)
                .map(e -> (T) e);
    }

    public void deleteById(String id) {
        queryExecutor.executeDeleteById(id, entityClass, entityTableName);
    }

    public void delete(Object entity) {
        deleteById(MacchiatoReflectionTools.getColumnIdValueFromObject(entity));
    }

}
