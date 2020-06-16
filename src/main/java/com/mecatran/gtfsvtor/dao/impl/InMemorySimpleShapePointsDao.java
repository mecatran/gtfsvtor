package com.mecatran.gtfsvtor.dao.impl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.mecatran.gtfsvtor.dao.ShapePointsDao;
import com.mecatran.gtfsvtor.model.GtfsShape;
import com.mecatran.gtfsvtor.model.GtfsShape.Id;
import com.mecatran.gtfsvtor.model.GtfsShapePoint;

public class InMemorySimpleShapePointsDao implements ShapePointsDao {

	private ListMultimap<GtfsShape.Id, GtfsShapePoint> shapePoints = ArrayListMultimap
			.create();

	@Override
	public void addShapePoint(GtfsShapePoint shapePoint) {
		shapePoints.put(shapePoint.getShapeId(), shapePoint);
	}

	@Override
	public void close() {
		// Sort shape points by point sequence
		Multimaps.asMap(shapePoints).values().forEach(points -> Collections
				.sort(points, GtfsShapePoint.POINT_SEQ_COMPARATOR));
	}

	@Override
	public int getShapePointsCount() {
		return shapePoints.size();
	}

	@Override
	public Stream<Id> getShapeIds() {
		return shapePoints.keySet().stream();
	}

	@Override
	public Optional<List<GtfsShapePoint>> getPointsOfShape(Id shapeId) {
		// Note: a multimap return an empty collection by default
		List<GtfsShapePoint> points = shapePoints.get(shapeId);
		return points.isEmpty() ? Optional.empty()
				: Optional.of(Collections.unmodifiableList(points));
	}

}
