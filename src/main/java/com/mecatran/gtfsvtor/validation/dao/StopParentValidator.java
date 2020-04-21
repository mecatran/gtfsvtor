package com.mecatran.gtfsvtor.validation.dao;

import com.mecatran.gtfsvtor.dao.ReadOnlyDao;
import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.model.GtfsStopType;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.InvalidReferenceError;
import com.mecatran.gtfsvtor.reporting.issues.InvalidStopParentError;
import com.mecatran.gtfsvtor.reporting.issues.MissingMandatoryValueError;
import com.mecatran.gtfsvtor.validation.DaoValidator;

public class StopParentValidator implements DaoValidator {

	@Override
	public void validate(DaoValidator.Context context) {
		ReadOnlyDao dao = context.getDao();
		ReportSink reportSink = context.getReportSink();

		dao.getStops().forEach(stop -> {
			GtfsStop parent = null;
			if (stop.getParentId() != null) {
				parent = dao.getStop(stop.getParentId());
				if (parent == null) {
					reportSink.report(new InvalidReferenceError(
							stop.getSourceInfo(), "parent_station",
							stop.getParentId().getInternalId(),
							GtfsStop.TABLE_NAME, "stop_id"));
				}
			}
			// Check stop child->parent reference
			switch (stop.getType()) {
			case STATION:
				// Parent should be null
				if (stop.getParentId() != null) {
					reportSink.report(
							new InvalidStopParentError(stop, parent, null));
				}
				break;
			case STOP:
				// Parent is optional; if defined it should be a station
				if (parent != null
						&& parent.getType() != GtfsStopType.STATION) {
					reportSink.report(new InvalidStopParentError(stop, parent,
							GtfsStopType.STATION));
				}
				break;
			case BOARDING_AREA:
			case ENTRANCE:
			case NODE:
				// Parent is mandatory
				if (parent == null) {
					// Missing
					reportSink.report(new MissingMandatoryValueError(
							stop.getSourceInfo(), "parent_station"));
				} else {
					GtfsStopType actualParentType = parent.getType();
					GtfsStopType expectedParentType = stop
							.getType() == GtfsStopType.BOARDING_AREA
									? GtfsStopType.STOP
									: GtfsStopType.STATION;
					if (actualParentType != expectedParentType) {
						// Actual parent type is not expected type
						reportSink.report(new InvalidStopParentError(stop,
								parent, expectedParentType));
					}
				}
			}
		});
	}
}
