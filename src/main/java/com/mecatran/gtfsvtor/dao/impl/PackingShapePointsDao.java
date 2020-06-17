package com.mecatran.gtfsvtor.dao.impl;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

import com.mecatran.gtfsvtor.dao.ShapePointsDao;
import com.mecatran.gtfsvtor.model.GtfsShape;
import com.mecatran.gtfsvtor.model.GtfsShapePoint;
import com.mecatran.gtfsvtor.model.impl.PackedShapePoints;

public class PackingShapePointsDao implements ShapePointsDao,
		ListPacker.PackerUnpacker<GtfsShape.Id, GtfsShapePoint, PackedShapePoints> {

	@FunctionalInterface
	public static interface AssertListener {
		public void check(List<GtfsShapePoint> before,
				List<GtfsShapePoint> after);
	}

	public static class DefaultContext implements PackedShapePoints.Context {
	}

	private ListPacker<GtfsShape.Id, GtfsShapePoint, PackedShapePoints> listPacker;
	private DefaultContext context = new DefaultContext();
	private static AssertListener assertListener = null;
	private boolean verbose = false;
	private boolean closed = false;

	public PackingShapePointsDao(int maxInterleaving) {
		this.listPacker = new ListPacker<>(this, maxInterleaving);
	}

	public PackingShapePointsDao withVerbose(boolean verbose) {
		this.verbose = verbose;
		return this;
	}

	@Override
	public void addShapePoint(GtfsShapePoint shapePoint) {
		if (closed)
			throw new RuntimeException(
					"Cannot re-open a closed PackingShapePointsDao. Implement this if needed.");
		GtfsShape.Id shapeId = shapePoint.getShapeId();
		listPacker.push(shapeId, shapePoint);
	}

	@Override
	public int getShapePointsCount() {
		closeIfNeeded();
		return listPacker.itemsCount();
	}

	@Override
	public Stream<GtfsShape.Id> getShapeIds() {
		closeIfNeeded();
		return listPacker.keys();
	}

	@Override
	public boolean hasShape(GtfsShape.Id shapeId) {
		closeIfNeeded();
		return listPacker.get(shapeId) != null;
	}

	@Override
	public Optional<List<GtfsShapePoint>> getPointsOfShape(
			GtfsShape.Id shapeId) {
		closeIfNeeded();
		PackedShapePoints psp = listPacker.get(shapeId);
		Optional<List<GtfsShapePoint>> shapePoints = psp == null
				? Optional.empty()
				: Optional.of(psp.getShapePoints(shapeId, context));
		return shapePoints;
	}

	@Override
	public PackedShapePoints pack(GtfsShape.Id shapeId,
			List<GtfsShapePoint> shapePoints) {
		Collections.sort(shapePoints, GtfsShapePoint.POINT_SEQ_COMPARATOR);
		PackedShapePoints packed = new PackedShapePoints(context, shapePoints);
		if (assertListener != null) {
			assertListener.check(shapePoints,
					packed.getShapePoints(shapeId, context));
		}
		return packed;
	}

	@Override
	public List<GtfsShapePoint> unpack(GtfsShape.Id shapeId,
			PackedShapePoints w) {
		return w.getShapePoints(shapeId, context);
	}

	/**
	 * @param enable True to enable assert mode: that is check if the
	 *        packed/unpacked shape points are the same as the original ones
	 *        before packing. Enable this only for testing, as this have a large
	 *        impact on performance.
	 */
	public static void setAssertListener(AssertListener assertListener) {
		PackingShapePointsDao.assertListener = assertListener;
	}

	private void closeIfNeeded() {
		if (closed)
			return;
		listPacker.close();
		if (verbose) {
			long nShapePoints = listPacker.itemsCount();
			long nShapes = listPacker.groupCount();
			long shapeBytes = nShapes * 8; // 1 pointer
			long dataBytes = listPacker.all().mapToInt(psp -> psp.getDataSize())
					.sum();
			long totalBytes = shapeBytes + dataBytes;
			System.out.println(String.format(Locale.US,
					"Packed %d points, %d shapes (%dk) in (%dk)", nShapePoints,
					nShapes, shapeBytes / 1024, dataBytes / 1024));
			System.out.println(String.format(Locale.US,
					"Total %dk. Avg bytes: %.2f per shape point, %.2f per shape",
					totalBytes / 1024, totalBytes * 1.0 / nShapePoints,
					totalBytes * 1.0 / nShapes));
		}
		closed = true;
	}
}