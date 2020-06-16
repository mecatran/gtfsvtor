package com.mecatran.gtfsvtor.dao;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.mecatran.gtfsvtor.model.GtfsShape;
import com.mecatran.gtfsvtor.model.GtfsShapePoint;

public interface ShapePointsDao {

	public void addShapePoint(GtfsShapePoint shapePoint);

	public void close();

	public int getShapePointsCount();

	public Stream<GtfsShape.Id> getShapeIds();

	public Optional<List<GtfsShapePoint>> getPointsOfShape(
			GtfsShape.Id shapeId);

	public default ShapePointsDao withVerbose(boolean verbose) {
		return this;
	}
}
