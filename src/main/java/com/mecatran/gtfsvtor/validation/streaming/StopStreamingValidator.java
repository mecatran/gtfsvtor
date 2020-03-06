package com.mecatran.gtfsvtor.validation.streaming;

import static com.mecatran.gtfsvtor.validation.impl.StreamingValidationUtils.checkNonNull;

import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.model.GtfsStopType;
import com.mecatran.gtfsvtor.validation.StreamingValidateType;
import com.mecatran.gtfsvtor.validation.StreamingValidator;

@StreamingValidateType(GtfsStop.class)
public class StopStreamingValidator implements StreamingValidator<GtfsStop> {

	@Override
	public void validate(Class<? extends GtfsStop> clazz, GtfsStop stop,
			StreamingValidator.Context context) {
		GtfsStopType stopType = stop.getType();
		boolean namePosMandatory = stopType == GtfsStopType.STOP
				|| stopType == GtfsStopType.STATION
				|| stopType == GtfsStopType.ENTRANCE;
		if (namePosMandatory) {
			checkNonNull(stop::getLat, "stop_lat", context);
			checkNonNull(stop::getLon, "stop_lon", context);
		}
		// Note: Parent station validity is checked in the reference validator
	}
}
