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
	private boolean closed = false;

	@Override
	public void addShapePoint(GtfsShapePoint shapePoint) {
		if (closed)
			throw new RuntimeException(
					"Cannot re-open a closed InMemorySimpleShapePointsDao. Implement this if needed.");
		shapePoints.put(shapePoint.getShapeId(), shapePoint);
	}

	@Override
	public int getShapePointsCount() {
		closeIfNeeded();
		return shapePoints.size();
	}

	@Override
	public Stream<GtfsShape.Id> getShapeIds() {
		closeIfNeeded();
		return shapePoints.keySet().stream();
	}

	@Override
	public boolean hasShape(GtfsShape.Id shapeId) {
		closeIfNeeded();
		return shapePoints.containsKey(shapeId);
	}

	@Override
	public Optional<List<GtfsShapePoint>> getPointsOfShape(Id shapeId) {
		closeIfNeeded();
		// Note: a multimap return an empty collection by default
		List<GtfsShapePoint> points = shapePoints.get(shapeId);
		return points.isEmpty() ? Optional.empty()
				: Optional.of(Collections.unmodifiableList(points));
	}

	private void closeIfNeeded() {
		if (closed)
			return;
		// Sort shape points by point sequence
		Multimaps.asMap(shapePoints).values().forEach(points -> Collections
				.sort(points, GtfsShapePoint.POINT_SEQ_COMPARATOR));
		closed = true;
	}
}
