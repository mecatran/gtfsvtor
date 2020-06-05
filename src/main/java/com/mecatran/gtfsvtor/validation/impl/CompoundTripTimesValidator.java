package com.mecatran.gtfsvtor.validation.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mecatran.gtfsvtor.model.GtfsTripAndTimes;
import com.mecatran.gtfsvtor.validation.DaoValidator.Context;
import com.mecatran.gtfsvtor.validation.TripTimesValidator;

public class CompoundTripTimesValidator
		implements TripTimesValidator {

	private List<? extends TripTimesValidator> validators;

	public CompoundTripTimesValidator(
			List<? extends TripTimesValidator> validators) {
		this.validators = new ArrayList<>(validators);
	}

	public List<? extends TripTimesValidator> getValidators() {
		return Collections.unmodifiableList(validators);
	}

	@Override
	public void start(Context context) {
		validators.forEach(v -> v.start(context));
	}

	@Override
	public void validate(Context context, GtfsTripAndTimes tripAndTimes) {
		validators.forEach(v -> v.validate(context, tripAndTimes));
	}

	@Override
	public void end(Context context) {
		validators.forEach(v -> v.end(context));
	}
}
