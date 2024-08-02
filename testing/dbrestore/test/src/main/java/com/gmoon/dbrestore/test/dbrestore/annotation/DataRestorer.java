package com.gmoon.dbrestore.test.dbrestore.annotation;

import com.gmoon.dbrestore.test.dbrestore.DatabaseRestoreExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@ExtendWith(DatabaseRestoreExtension.class)
@Target(value = ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface DataRestorer {

}
