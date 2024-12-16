package org.gabrielgavrilov.macchiato;

import org.gabrielgavrilov.macchiato.annotations.Column;
import org.gabrielgavrilov.macchiato.annotations.Table;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class MacchiatoRepository<T> {

    private final Class<T> ENTITY = getGenericType();
    private final DataSource DATA_SOURCE = new DataSource();

    public MacchiatoRepository() {
    }


    public List<T> getAll() {
        List<T> entities = new ArrayList<>();
        String table = this.ENTITY.getAnnotation(Table.class).name();
        List<Field> fields = new ArrayList<>();

        for(Field field : this.ENTITY.getDeclaredFields()) {
            fields.add(field);
        }

        try {
            ResultSet rs = this.DATA_SOURCE.executeQuery(QueryBuilder.getAll(table));

            while(rs.next()) {
                T entity = (T) this.ENTITY.getDeclaredConstructor().newInstance();

                for(Field field : fields) {
                    field.setAccessible(true);
                    String columnName = field.getAnnotation(Column.class).name();
                    Object value = rs.getObject(columnName);
                    field.set(entity, value);
                }

                entities.add(entity);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return entities;
    }

//    public T getById() {
//        T entity = null;
//        String table = this.ENTITY.getAnnotation(Table.class).name();
//        List<Field>
//
//        return entity;
//    }

    public void save(Object entity) {
        String table = this.ENTITY.getAnnotation(Table.class).name();
        List<String> fields = new ArrayList<>();
        List<String> values = new ArrayList<>();

        try {
            for(Field field : this.ENTITY.getDeclaredFields()) {
                fields.add(field.getAnnotation(Column.class).name());
                values.add(field.get(entity).toString());
            }

            this.DATA_SOURCE.executeQuery(QueryBuilder.save(table, fields, values));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    private Class<T> getGenericType() {
        Class<?> clazz = this.getClass();
        Type type = clazz.getGenericSuperclass();
        ParameterizedType pt = (ParameterizedType) type;
        return (Class<T>)pt.getActualTypeArguments()[0];
    }

}
