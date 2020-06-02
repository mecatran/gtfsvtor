package com.mecatran.gtfsvtor.validation;

import com.mecatran.gtfsvtor.model.GtfsTripAndTimes;
import com.mecatran.gtfsvtor.validation.DaoValidator.Context;

public interface TripTimesValidator {

	public default void start(Context context) {
	};

	/**
	 * Validate a trip and associated trip times. This method is guaranteed to
	 * be called with trips grouped by route.
	 */
	public void validate(Context context, GtfsTripAndTimes tripAndTimes);

	public default void end(Context context) {
	};
}
