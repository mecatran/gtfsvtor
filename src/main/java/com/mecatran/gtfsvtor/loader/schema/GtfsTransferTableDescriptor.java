package com.mecatran.gtfsvtor.loader.schema;

import com.mecatran.gtfsvtor.loader.DataRowConverter;
import com.mecatran.gtfsvtor.model.GtfsObject;
import com.mecatran.gtfsvtor.model.GtfsRoute;
import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.model.GtfsTransfer;
import com.mecatran.gtfsvtor.model.GtfsTrip;

@TableDescriptorPolicy(objectClass = GtfsTransfer.class, tableName = GtfsTransfer.TABLE_NAME, mandatory = false, mandatoryColumns = {
		"from_stop_id", "to_stop_id", "transfer_type" })
public class GtfsTransferTableDescriptor implements GtfsTableDescriptor {

	@Override
	public GtfsObject<?> parseAndSave(DataRowConverter erow, Context context) {
		GtfsTransfer.Builder builder = new GtfsTransfer.Builder();
		builder.withFromStopId(GtfsStop.id(erow.getString("from_stop_id")))
				.withToStopId(GtfsStop.id(erow.getString("to_stop_id")))
				.withFromRouteId(GtfsRoute.id(erow.getString("from_route_id")))
				.withToRouteId(GtfsRoute.id(erow.getString("to_route_id")))
				.withFromTripId(GtfsTrip.id(erow.getString("from_trip_id")))
				.withToTripId(GtfsTrip.id(erow.getString("to_trip_id")))
				.withTransferType(erow.getTransferType("transfer_type"))
				.withMinTransferTime(
						erow.getInteger("min_transfer_time", false));
		GtfsTransfer transfer = builder.build();
		context.getAppendableDao().addTransfer(transfer,
				context.getSourceContext());
		return transfer;
	}
}
