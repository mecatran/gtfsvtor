package com.mecatran.gtfsvtor.validation.streaming;

import static com.mecatran.gtfsvtor.validation.impl.StreamingValidationUtils.checkFormat;

import com.mecatran.gtfsvtor.dao.ReadOnlyDao;
import com.mecatran.gtfsvtor.geospatial.GeoCoordinates;
import com.mecatran.gtfsvtor.geospatial.Geodesics;
import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.model.GtfsStopType;
import com.mecatran.gtfsvtor.model.GtfsTransfer;
import com.mecatran.gtfsvtor.model.GtfsTransferType;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.InvalidFieldValueIssue;
import com.mecatran.gtfsvtor.reporting.issues.InvalidReferenceError;
import com.mecatran.gtfsvtor.reporting.issues.MissingMandatoryValueError;
import com.mecatran.gtfsvtor.reporting.issues.TooFastWalkingSpeed;
import com.mecatran.gtfsvtor.reporting.issues.UselessValueWarning;
import com.mecatran.gtfsvtor.reporting.issues.WrongTransferStopTypeError;
import com.mecatran.gtfsvtor.validation.ConfigurableOption;
import com.mecatran.gtfsvtor.validation.StreamingValidateType;
import com.mecatran.gtfsvtor.validation.StreamingValidator;

@StreamingValidateType(GtfsTransfer.class)
public class TransferStreamingValidator
		implements StreamingValidator<GtfsTransfer> {

	@ConfigurableOption(description = "Maximum transfer time, in seconds, above which a warning is generated")
	private int maxTransferTimeSecWarning = 3 * 60 * 60;

	@ConfigurableOption(description = "Maximum transfer time, in seconds, above which an error is generated")
	private int maxTransferTimeSecError = 24 * 60 * 60;

	@ConfigurableOption(description = "Maximum distance between stops, in meters, above which a warning is generated")
	private double maxDistanceMetersWarning = 2000;

	@ConfigurableOption(description = "Maximum distance between stops, in meters, above which an error is generated")
	private double maxDistanceMetersError = 10000;

	@ConfigurableOption(description = "Fast walking speed, in meters/seconds, above which an issue is generated")
	private double fastWalkingSpeedMps = 2.0;

	@ConfigurableOption(description = "Fast walking speed slack time, in seconds (added to transfer time before speed computation)")
	private int walkingTimeSlackSec = 120;

	@Override
	public void validate(Class<? extends GtfsTransfer> clazz,
			GtfsTransfer transfer, StreamingValidator.Context context) {
		ReportSink reportSink = context.getReportSink();
		// stop from / to ID is primary key and tested by DAO
		if (transfer.getMinTransferTime() != null) {
			checkFormat(transfer::getMinTransferTime, "min_transfer_time",
					context, t -> t >= 0, "positive integer");
			if (transfer.getNonNullType() == GtfsTransferType.TIMED) {
				if (maxTransferTimeSecError > 0 && transfer
						.getMinTransferTime() > maxTransferTimeSecError) {
					reportSink.report(new InvalidFieldValueIssue(
							context.getSourceInfo(),
							Integer.toString(transfer.getMinTransferTime()),
							"Suspiciously large time", "min_transfer_time")
									.withSeverity(ReportIssueSeverity.ERROR));
				} else if (maxTransferTimeSecWarning > 0 && transfer
						.getMinTransferTime() > maxTransferTimeSecWarning) {
					reportSink.report(new InvalidFieldValueIssue(
							context.getSourceInfo(),
							Integer.toString(transfer.getMinTransferTime()),
							"Suspiciously large time", "min_transfer_time")
									.withSeverity(ReportIssueSeverity.WARNING));
				}
			}
		}

		ReadOnlyDao dao = context.getPartialDao();

		// check from/to stops reference
		GtfsStop fromStop = null;
		GtfsStop toStop = null;
		if (transfer.getFromStopId() != null) {
			fromStop = dao.getStop(transfer.getFromStopId());
			if (fromStop == null) {
				reportSink.report(new InvalidReferenceError(
						context.getSourceInfo(), "from_stop_id",
						transfer.getFromStopId().getInternalId(),
						GtfsStop.TABLE_NAME, "stop_id"));
			} else {
				if (fromStop.getType() != GtfsStopType.STOP
						&& fromStop.getType() != GtfsStopType.STATION) {
					reportSink.report(new WrongTransferStopTypeError(
							context.getSourceInfo(), transfer, fromStop,
							"from_stop_id"));
				}
			}
		}
		if (transfer.getToStopId() != null) {
			toStop = dao.getStop(transfer.getToStopId());
			if (toStop == null) {
				reportSink.report(new InvalidReferenceError(
						context.getSourceInfo(), "to_stop_id",
						transfer.getToStopId().getInternalId(),
						GtfsStop.TABLE_NAME, "stop_id"));
			} else {
				if (toStop.getType() != GtfsStopType.STOP
						&& toStop.getType() != GtfsStopType.STATION) {
					reportSink.report(new WrongTransferStopTypeError(
							context.getSourceInfo(), transfer, toStop,
							"to_stop_id"));
				}
			}
		}
		// Check from-to stop distance and walk speed
		if (fromStop != null && toStop != null) {
			GeoCoordinates p1 = fromStop.getCoordinates();
			GeoCoordinates p2 = toStop.getCoordinates();
			if (p1 != null && p2 != null) {
				double d = Geodesics.fastDistanceMeters(p1, p2);
				ReportIssueSeverity severity = null;
				if (maxDistanceMetersError > 0 && d > maxDistanceMetersError) {
					severity = ReportIssueSeverity.ERROR;
				} else if (maxDistanceMetersWarning > 0
						&& d > maxDistanceMetersWarning) {
					severity = ReportIssueSeverity.WARNING;
				}
				if (severity != null) {
					// TODO Make a specific error class?
					reportSink.report(new InvalidFieldValueIssue(
							context.getSourceInfo(),
							String.format("%.2f meters", d),
							"Suspiciously large transfer distance between stops",
							"from_stop_id", "to_stop_id")
									.withSeverity(severity));
				}

				if (transfer.getMinTransferTime() != null
						&& transfer.getMinTransferTime() > 0) {
					double speedMps = d / (transfer.getMinTransferTime()
							+ walkingTimeSlackSec);
					if (speedMps > fastWalkingSpeedMps) {
						reportSink.report(new TooFastWalkingSpeed(
								context.getSourceInfo(), fromStop, toStop, d,
								speedMps, fastWalkingSpeedMps,
								ReportIssueSeverity.WARNING));
					}
				}
			}
		}

		// check from/to route reference
		GtfsRoute fromRoute = null;
		GtfsRoute toRoute = null;
		if (transfer.getFromRouteId() != null) {
			fromRoute = dao.getRoute(transfer.getFromRouteId());
			if (fromRoute == null) {
				reportSink
						.report(new InvalidReferenceError(context.getSourceInfo(), "from_route_id",
								transfer.getFromRouteId().getInternalId(), GtfsRoute.TABLE_NAME,
								"route_id"));
			}
		}
		if (transfer.getToRouteId() != null) {
			toRoute = dao.getRoute(transfer.getToRouteId());
			if (toRoute == null) {
				reportSink.report(new InvalidReferenceError(context.getSourceInfo(), "to_route_id",
						transfer.getToRouteId().getInternalId(), GtfsRoute.TABLE_NAME, "route_id"));
			}
		}

		// check from/to route reference
		GtfsTrip fromTrip = null;
		GtfsTrip toTrip = null;
		if (transfer.getFromTripId() != null) {
			fromTrip = dao.getTrip(transfer.getFromTripId());
			if (fromTrip == null) {
				reportSink.report(new InvalidReferenceError(context.getSourceInfo(), "from_trip_id",
						transfer.getFromTripId().getInternalId(), GtfsRoute.TABLE_NAME, "trip_id"));
			}
		}
		if (transfer.getToTripId() != null) {
			toTrip = dao.getTrip(transfer.getToTripId());
			if (toTrip == null) {
				reportSink.report(new InvalidReferenceError(context.getSourceInfo(), "to_trip_id",
						transfer.getToTripId().getInternalId(), GtfsTrip.TABLE_NAME, "trip_id"));
			}
		}
		
		if (transfer.getNonNullType() == GtfsTransferType.TIMED) {
			// min transfer time should be present for TIMED type
			if (transfer.getMinTransferTime() == null) {
				reportSink.report(new MissingMandatoryValueError(
						context.getSourceInfo(), "min_transfer_time"));
			}
		} else {
			// min transfer time should not be present for other types
			if (transfer.getMinTransferTime() != null) {
				reportSink.report(new UselessValueWarning(
						context.getSourceInfo(), "min_transfer_time",
						Integer.toString(transfer.getMinTransferTime()),
						"This field is only useful if transfer_type=2"));
			}
		}
	}
}
