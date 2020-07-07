package com.njwd.annotation;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ExcelCell {

    int index();

    boolean redundancy() default false;

}
