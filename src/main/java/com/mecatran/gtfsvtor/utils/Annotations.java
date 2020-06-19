package com.mecatran.gtfsvtor.utils;

import java.lang.annotation.Annotation;
import java.util.function.Function;

import com.mecatran.gtfsvtor.loader.schema.TableDescriptorPolicy;

public class Annotations {

	public static <TA extends Annotation, TR, TO> TR getAnnotation(
			Class<TA> annotationClass, Class<TR> returnClass, TO obj,
			Function<TA, TR> getter) {
		TR ret = getAnnotation(annotationClass, returnClass, obj, getter, null);
		if (ret == null) {
			throw new RuntimeException(
					"Must either annotate class " + obj.getClass() + " with "
							+ annotationClass + " or override method.");
		} else {
			return ret;
		}
	}

	public static <TA extends Annotation, TR, TO> TR getAnnotation(
			Class<TA> annotationClass, Class<TR> returnClass, TO obj,
			Function<TA, TR> getter, TR defaultValue) {
		Class<? extends Object> objClass = obj.getClass();
		if (objClass.isAnnotationPresent(annotationClass)) {
			TA annotation = objClass.getAnnotation(annotationClass);
			return getter.apply(annotation);
		} else {
			return defaultValue;
		}
	}

}
