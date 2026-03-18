package org.gabrielgavrilov.macchiato;

import org.gabrielgavrilov.macchiato.annotations.Column;
import org.gabrielgavrilov.macchiato.annotations.Id;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MacchiatoReflectionTools {

    /**
     * Returns a list of fields that belong to the generic entity of the class.
     * @return list of fields that belong to the generic entity.
     */
    public static List<Field> getListOfFieldsFromClass(Class<?> clazz) {
        return List.of(clazz.getDeclaredFields());
    }

    /**
     * Returns the generic type of the class
     * @return generic type {@code T} of the repository class
     */
    public static Class<?> getGenericClassType(Class<?> clazz) {
        Type type = clazz.getGenericSuperclass();
        ParameterizedType pt = (ParameterizedType) type;
        return (Class<?>)pt.getActualTypeArguments()[0];
    }

    /**
     * Returns a list of column names that belong to a given class.
     * @param clazz class to retrieve column names from.
     * @return list of column names that belong to a given class.
     */
    public static List<String> getAllColumnNamesFromClass(Class<?> clazz) {
        List<String> columns = new ArrayList<>();
        for(Field field : clazz.getDeclaredFields()) {
            if(field.isAnnotationPresent(Column.class)) {
                columns.add(field.getAnnotation(Column.class).name());
            }
        }

        return columns;
    }

    /**
     * Maps column names and their values from a given object to a HashMap.
     * @param entity a constructed object entity to map its column names and their associated values.
     * @return HashMap of column names and their associated values
     * @throws Exception
     */
    public static HashMap<String, String> mapColumnNamesToValuesFromObject(Object entity) {
        try {
            HashMap<String, String> columnsAndValues = new HashMap<>();

            for (Field field : entity.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(Column.class)) {
                    columnsAndValues.put(field.getAnnotation(Column.class).name(), String.valueOf(field.get(entity)));
                }
            }
            return columnsAndValues;
        } catch (IllegalAccessException e) {
            // Todo: fix
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the entity id column that belongs to the generic entity of the class.
     * @return string id column
     */
    public static String getColumnIdNameFromClass(Class<?> clazz) {
        for(Field field : clazz.getDeclaredFields()) {
            if(field.isAnnotationPresent(Id.class) && field.isAnnotationPresent(Column.class)) {
                return field.getAnnotation(Column.class).name();
            }
        }
        return null;
    }

    /**
     * Returns the id value from a given object.
     * @param entity constructed entity object to obtain an id from.
     * @return string id value
     * @throws Exception
     */
    public static String getColumnIdValueFromObject(Object entity) {
        try {
            for(Field field : entity.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(Id.class) && field.isAnnotationPresent(Column.class)) {
                    field.setAccessible(true);
                    return String.valueOf(field.get(entity));
                }
            }
            return null;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


}
