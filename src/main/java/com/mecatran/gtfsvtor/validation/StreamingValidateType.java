package com.mecatran.gtfsvtor.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.mecatran.gtfsvtor.model.GtfsObject;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface StreamingValidateType {

	Class<? extends GtfsObject<?>> value();
}
