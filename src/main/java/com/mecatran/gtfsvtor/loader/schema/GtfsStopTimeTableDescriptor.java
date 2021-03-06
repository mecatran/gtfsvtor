package com.mecatran.gtfsvtor.loader.schema;

import com.mecatran.gtfsvtor.loader.DataRowConverter;
import com.mecatran.gtfsvtor.loader.DataRowConverter.Requiredness;
import com.mecatran.gtfsvtor.model.GtfsObject;
import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.model.GtfsStopTime;
import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.model.impl.SimpleGtfsStopTime;

@TableDescriptorPolicy(objectClass = GtfsStopTime.class, tableName = GtfsStopTime.TABLE_NAME, mandatory = true, mandatoryColumns = {
		"trip_id", "arrival_time", "departure_time", "stop_id",
		"stop_sequence" })
public class GtfsStopTimeTableDescriptor implements GtfsTableDescriptor {

	@Override
	public GtfsObject<?> parseAndSave(DataRowConverter erow, Context context) {
		GtfsStopTime.Builder builder = new SimpleGtfsStopTime.Builder();
		builder.withTripId(GtfsTrip.id(erow.getString("trip_id")))
				.withArrivalTime(erow.getLogicalTime("arrival_time",
						Requiredness.OPTIONAL))
				.withDepartureTime(erow.getLogicalTime("departure_time",
						Requiredness.OPTIONAL))
				.withStopId(GtfsStop.id(erow.getString("stop_id")))
				.withStopSequence(erow.getTripStopSequence("stop_sequence"))
				.withStopHeadsign(erow.getString("stop_headsign"))
				.withPickupType(erow.getPickupType("pickup_type"))
				.withDropoffType(erow.getDropoffType("drop_off_type"))
				.withShapeDistTraveled(erow.getDouble("shape_dist_traveled",
						Requiredness.OPTIONAL))
				.withTimepoint(erow.getTimepoint("timepoint"));
		GtfsStopTime stopTime = builder.build();
		context.getAppendableDao().addStopTime(stopTime,
				context.getSourceContext());
		return stopTime;
	}
}
