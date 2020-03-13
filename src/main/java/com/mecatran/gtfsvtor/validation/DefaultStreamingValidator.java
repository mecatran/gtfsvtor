package com.mecatran.gtfsvtor.validation;

import java.util.List;

import com.mecatran.gtfsvtor.model.GtfsObject;
import com.mecatran.gtfsvtor.validation.impl.CompoundStreamingValidator;
import com.mecatran.gtfsvtor.validation.impl.ValidatorInjector;

public class DefaultStreamingValidator
		implements StreamingValidator<GtfsObject<?>> {

	private CompoundStreamingValidator compound;

	public DefaultStreamingValidator(ValidatorConfig config) {
		@SuppressWarnings("unchecked")
		List<? extends StreamingValidator<GtfsObject<?>>> validators = (List<? extends StreamingValidator<GtfsObject<?>>>) ValidatorInjector
				.getStreamingValidatorInjector().scanPackageAndInject(config);
		compound = new CompoundStreamingValidator(validators);
	}

	@Override
	public void validate(Class<? extends GtfsObject<?>> clazz,
			GtfsObject<?> object, Context context) {
		compound.validate(clazz, object, context);
	}
}
