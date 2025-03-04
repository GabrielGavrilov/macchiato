package org.gabrielgavrilov.macchiato.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JoinColumn {
    public String table() default "";
    public String column() default "";
    public Class<?> referencedClass() default Object.class;
}
