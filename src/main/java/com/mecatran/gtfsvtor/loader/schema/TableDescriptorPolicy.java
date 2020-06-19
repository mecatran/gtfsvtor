package com.mecatran.gtfsvtor.loader.schema;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.mecatran.gtfsvtor.model.GtfsObject;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TableDescriptorPolicy {

	Class<? extends GtfsObject<?>> objectClass();

	String tableName();

	boolean mandatory() default false;

	String[] mandatoryColumns() default {};
}
