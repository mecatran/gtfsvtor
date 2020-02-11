package com.mecatran.gtfsvtor.validation.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mecatran.gtfsvtor.model.GtfsObject;
import com.mecatran.gtfsvtor.validation.StreamingValidateType;
import com.mecatran.gtfsvtor.validation.StreamingValidator;

public class CompoundStreamingValidator
		implements StreamingValidator<GtfsObject<?>> {

	private Map<Class<? extends GtfsObject<?>>, StreamingValidator<? extends GtfsObject<?>>> validators = new HashMap<>();

	public CompoundStreamingValidator(
			List<? extends StreamingValidator<GtfsObject<?>>> validators) {
		for (StreamingValidator<GtfsObject<?>> validator : validators) {
			@SuppressWarnings("rawtypes")
			Class<? extends StreamingValidator> clazz = validator.getClass();
			if (clazz.isAnnotationPresent(StreamingValidateType.class)) {
				Class<? extends GtfsObject<?>> objectClazz = clazz
						.getAnnotation(StreamingValidateType.class).value();
				this.validators.put(objectClazz, validator);
			} else {
				System.out.println("Unable to register "
						+ StreamingValidator.class.getSimpleName() + " "
						+ clazz.getSimpleName()
						+ ", it should be annotated with @"
						+ StreamingValidateType.class.getSimpleName());
			}
		}
	}

	@Override
	public void validate(GtfsObject<?> object, Context context) {
		@SuppressWarnings("unchecked")
		StreamingValidator<GtfsObject<?>> validator = (StreamingValidator<GtfsObject<?>>) validators
				.get(object.getClass());
		if (validator != null) {
			validator.validate(object, context);
		}
	}
}
