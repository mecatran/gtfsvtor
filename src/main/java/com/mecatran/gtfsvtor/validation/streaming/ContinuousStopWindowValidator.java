package com.mecatran.gtfsvtor.validation.streaming;

import java.util.Optional;

import com.mecatran.gtfsvtor.model.DataObjectSourceRef;
import com.mecatran.gtfsvtor.model.GtfsContinuousStop;
import com.mecatran.gtfsvtor.model.GtfsRoute;
import com.mecatran.gtfsvtor.model.GtfsStopTime;
import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.reporting.issues.InvalidFieldValueIssue;
import com.mecatran.gtfsvtor.validation.StreamingValidateType;
import com.mecatran.gtfsvtor.validation.StreamingValidator;

@StreamingValidateType(GtfsStopTime.class)
public class ContinuousStopWindowValidator implements StreamingValidator<GtfsStopTime> {

	@Override
	public void validate(Class<? extends GtfsStopTime> clazz,
			GtfsStopTime stopTime, Context context) {
		String start = context.getFieldValue("start_pickup_drop_off_window");
		String end = context.getFieldValue("end_pickup_drop_off_window");
		if ((start == null || start.isEmpty()) && (end == null || end.isEmpty()))
			return;

		DataObjectSourceRef row = context.getSourceRef();
		check(context, row, "continuous_pickup", stopTime.getContinuousPickup(), row);
		check(context, row, "continuous_drop_off", stopTime.getContinuousDropoff(), row);
		GtfsTrip trip = stopTime.getTripId() == null ? null
				: context.getPartialDao().getTrip(stopTime.getTripId());
		GtfsRoute route = trip == null || trip.getRouteId() == null ? null
				: context.getPartialDao().getRoute(trip.getRouteId());
		if (route != null) {
			check(context, route.getSourceRef(), "continuous_pickup", route.getContinuousPickup(), row);
			check(context, route.getSourceRef(), "continuous_drop_off", route.getContinuousDropoff(), row);
		}
	}

	private void check(Context context, DataObjectSourceRef source, String field,
			Optional<GtfsContinuousStop> value, DataObjectSourceRef windowRow) {
		if (value.orElse(GtfsContinuousStop.NONE) != GtfsContinuousStop.NONE) {
			context.getReportSink().report(new InvalidFieldValueIssue(source,
					Integer.toString(value.get().getValue()),
					"Must be 1 or empty when a pickup/drop-off window is set in "
							+ windowRow, field));
		}
	}
}
