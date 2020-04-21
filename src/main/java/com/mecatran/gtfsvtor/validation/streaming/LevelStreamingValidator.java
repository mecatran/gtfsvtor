package com.mecatran.gtfsvtor.validation.streaming;

import com.mecatran.gtfsvtor.model.GtfsLevel;
import com.mecatran.gtfsvtor.validation.StreamingValidateType;
import com.mecatran.gtfsvtor.validation.StreamingValidator;

@StreamingValidateType(GtfsLevel.class)
public class LevelStreamingValidator implements StreamingValidator<GtfsLevel> {

	@Override
	public void validate(Class<? extends GtfsLevel> clazz, GtfsLevel pathway,
			StreamingValidator.Context context) {
		// ID is tested by DAO layer, mandatory fields by loader
		// TODO other checks?
	}
}
