package com.mecatran.gtfsvtor.loader.schema;

import com.mecatran.gtfsvtor.loader.DataRowConverter;
import com.mecatran.gtfsvtor.model.GtfsObject;
import com.mecatran.gtfsvtor.model.GtfsShape;
import com.mecatran.gtfsvtor.model.GtfsShapePoint;
import com.mecatran.gtfsvtor.model.impl.SimpleGtfsShapePoint;

@TableDescriptorPolicy(objectClass = GtfsShapePoint.class, tableName = GtfsShapePoint.TABLE_NAME, mandatory = false, mandatoryColumns = {
		"shape_id", "shape_pt_lat", "shape_pt_lon", "shape_pt_sequence" })
public class GtfsShapePointTableDescriptor implements GtfsTableDescriptor {

	@Override
	public GtfsObject<?> parseAndSave(DataRowConverter erow, Context context) {
		GtfsShapePoint.Builder builder = new SimpleGtfsShapePoint.Builder();
		builder.withShapeId(GtfsShape.id(erow.getString("shape_id")))
				.withCoordinates(erow.getDouble("shape_pt_lat", true),
						erow.getDouble("shape_pt_lon", true))
				.withPointSequence(
						erow.getShapePointSequence("shape_pt_sequence"))
				.withShapeDistTraveled(
						erow.getDouble("shape_dist_traveled", false));
		GtfsShapePoint shapePoint = builder.build();
		context.getAppendableDao().addShapePoint(shapePoint,
				context.getSourceContext());
		return shapePoint;
	}
}
