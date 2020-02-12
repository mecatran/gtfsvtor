package com.mecatran.gtfsvtor.validation.streaming;

import static com.mecatran.gtfsvtor.validation.impl.StreamingValidationUtils.checkNonNull;

import com.mecatran.gtfsvtor.dao.ReadOnlyDao;
import com.mecatran.gtfsvtor.model.GtfsCalendar;
import com.mecatran.gtfsvtor.model.GtfsRoute;
import com.mecatran.gtfsvtor.model.GtfsShapePoint;
import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.InvalidReferenceError;
import com.mecatran.gtfsvtor.validation.StreamingValidateType;
import com.mecatran.gtfsvtor.validation.StreamingValidator;

@StreamingValidateType(GtfsTrip.class)
public class TripStreamingValidator implements StreamingValidator<GtfsTrip> {

	@Override
	public void validate(Class<? extends GtfsTrip> clazz, GtfsTrip trip,
			StreamingValidator.Context context) {
		ReportSink reportSink = context.getReportSink();
		ReadOnlyDao dao = context.getPartialDao();
		checkNonNull(trip::getRouteId, "route_id", context);
		checkNonNull(trip::getServiceId, "service_id", context);
		// Check trip->route reference
		if (trip.getRouteId() != null
				&& dao.getRoute(trip.getRouteId()) == null) {
			reportSink.report(new InvalidReferenceError(trip.getSourceInfo(),
					"route_id", trip.getRouteId().getInternalId(),
					GtfsRoute.TABLE_NAME, "route_id"));
		}
		// Check trip->calendar reference
		if (trip.getServiceId() != null
				&& dao.getCalendar(trip.getServiceId()) == null
				&& dao.getCalendarDates(trip.getServiceId()).isEmpty()) {
			reportSink.report(new InvalidReferenceError(trip.getSourceInfo(),
					"service_id",
					trip.getServiceId() == null ? null
							: trip.getServiceId().getInternalId(),
					GtfsCalendar.TABLE_NAME, "service_id"));
		}
		// Check trip->shape reference
		if (trip.getShapeId() != null && !dao.hasShape(trip.getShapeId())) {
			reportSink.report(
					new InvalidReferenceError(trip.getSourceInfo(), "shape_id",
							trip.getShapeId() == null ? null
									: trip.getShapeId().getInternalId(),
							GtfsShapePoint.TABLE_NAME, "shape_id"));
		}
	}
}
