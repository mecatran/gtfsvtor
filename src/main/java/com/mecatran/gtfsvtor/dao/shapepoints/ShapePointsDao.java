package com.mecatran.gtfsvtor.dao.shapepoints;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.mecatran.gtfsvtor.model.GtfsShape;
import com.mecatran.gtfsvtor.model.GtfsShapePoint;

public interface ShapePointsDao {

	public void addShapePoint(GtfsShapePoint shapePoint);

	public int getShapePointsCount();

	public Stream<GtfsShape.Id> getShapeIds();

	public boolean hasShape(GtfsShape.Id shapeId);

	public Optional<List<GtfsShapePoint>> getPointsOfShape(
			GtfsShape.Id shapeId);

	public default ShapePointsDao withVerbose(boolean verbose) {
		return this;
	}
}
