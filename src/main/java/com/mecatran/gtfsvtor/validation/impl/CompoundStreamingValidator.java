package com.mecatran.gtfsvtor.validation.impl;

import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.mecatran.gtfsvtor.model.GtfsObject;
import com.mecatran.gtfsvtor.validation.StreamingValidateType;
import com.mecatran.gtfsvtor.validation.StreamingValidator;

public class CompoundStreamingValidator
		implements StreamingValidator<GtfsObject<?>> {

	private ListMultimap<Class<? extends GtfsObject<?>>, StreamingValidator<? extends GtfsObject<?>>> validators = ArrayListMultimap
			.create();

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
	@SuppressWarnings("unchecked")
	public void validate(Class<? extends GtfsObject<?>> clazz,
			GtfsObject<?> object, Context context) {
		List<StreamingValidator<? extends GtfsObject<?>>> vtors = validators
				.get(clazz);
		for (StreamingValidator<? extends GtfsObject<?>> vtor : vtors) {
			StreamingValidator<GtfsObject<?>> vtor2 = (StreamingValidator<GtfsObject<?>>) vtor;
			vtor2.validate(clazz, object, context);
		}
	}
}
