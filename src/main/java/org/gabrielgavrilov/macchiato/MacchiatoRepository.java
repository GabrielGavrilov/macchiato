package org.gabrielgavrilov.macchiato;

import org.gabrielgavrilov.macchiato.annotations.Column;
import org.gabrielgavrilov.macchiato.annotations.Table;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class MacchiatoRepository<T> {

    private Controller _CONTROLLER;

    public MacchiatoRepository() {
        Class<T> clazz = getGenericType();
        if(clazz.isAnnotationPresent(Table.class)) {

            for(Field field : clazz.getDeclaredFields()) {
                if(field.isAnnotationPresent(Column.class)) {
                    Column column = field.getAnnotation(Column.class);
                    System.out.println(column.name());
                }
            }

        }
    }

    private Class<T> getGenericType() {
        Class<?> clazz = this.getClass();
        Type type = clazz.getGenericSuperclass();
        ParameterizedType pt = (ParameterizedType) type;
        return (Class<T>)pt.getActualTypeArguments()[0];
    }

}
