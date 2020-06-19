package com.mecatran.gtfsvtor.loader.schema;

import com.mecatran.gtfsvtor.loader.DataRowConverter;
import com.mecatran.gtfsvtor.model.GtfsObject;
import com.mecatran.gtfsvtor.model.GtfsPathway;
import com.mecatran.gtfsvtor.model.GtfsStop;

@TableDescriptorPolicy(objectClass = GtfsPathway.class, tableName = GtfsPathway.TABLE_NAME, mandatory = false, mandatoryColumns = {
		"pathway_id", "from_stop_id", "to_stop_id", "pathway_mode",
		"is_bidirectional" })
public class GtfsPathwayTableDescriptor implements GtfsTableDescriptor {

	@Override
	public GtfsObject<?> parseAndSave(DataRowConverter erow, Context context) {
		GtfsPathway.Builder builder = new GtfsPathway.Builder(
				erow.getString("pathway_id"));
		builder.withFromStopId(GtfsStop.id(erow.getString("from_stop_id")))
				.withToStopId(GtfsStop.id(erow.getString("to_stop_id")))
				.withPathwayMode(erow.getPathwayMode("pathway_mode"))
				.withBidirectional(erow.getDirectionality("is_bidirectional"))
				.withLength(erow.getDouble("length", false))
				.withTraversalTime(erow.getInteger("traversal_time", false))
				.withStairCount(erow.getInteger("stair_count", false))
				.withMaxSlope(erow.getDouble("max_slope", false))
				.withMinWitdth(erow.getDouble("min_width", false))
				.withSignpostedAs(erow.getString("signposted_as"))
				.withReversedSignpostedAs(
						erow.getString("reversed_signposted_as"));
		GtfsPathway pathway = builder.build();
		context.getAppendableDao().addPathway(pathway,
				context.getSourceContext());
		return pathway;
	}
}
