package com.mecatran.gtfsvtor.dao.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.mecatran.gtfsvtor.dao.ShapePointsDao;
import com.mecatran.gtfsvtor.model.GtfsShape;
import com.mecatran.gtfsvtor.model.GtfsShapePoint;

/**
 * Same concept as AutoSwitchStopTimesDao.
 */
public class AutoSwitchShapePointDao implements ShapePointsDao {

	private PackingShapePointsDao pspDao;
	private PackingUnsortedShapePointsDao puspDao;
	private ShapePointsDao currentDao;
	private boolean verbose = false;

	public AutoSwitchShapePointDao(int maxInterleaving) {
		pspDao = new PackingShapePointsDao(maxInterleaving)
				.withInterleavingOverflowCallback(this::handleOverflow);
		puspDao = null;
		currentDao = pspDao;
	}

	@Override
	public void addShapePoint(GtfsShapePoint shapePoint) {
		currentDao.addShapePoint(shapePoint);
	}

	@Override
	public int getShapePointsCount() {
		return currentDao.getShapePointsCount();
	}

	@Override
	public Stream<GtfsShape.Id> getShapeIds() {
		return currentDao.getShapeIds();
	}

	@Override
	public boolean hasShape(GtfsShape.Id shapeId) {
		return currentDao.hasShape(shapeId);
	}

	@Override
	public Optional<List<GtfsShapePoint>> getPointsOfShape(
			com.mecatran.gtfsvtor.model.GtfsShape.Id shapeId) {
		return currentDao.getPointsOfShape(shapeId);
	}

	@Override
	public ShapePointsDao withVerbose(boolean verbose) {
		this.verbose = verbose;
		currentDao.withVerbose(verbose);
		return this;
	}

	private boolean handleOverflow(int n) {
		System.out.println(
				"Interleaving shape points overflow detected. Switching to relevant DAO implementation to better handle this.\nThis will increase memory consumption, though.");
		pspDao.withVerbose(false);
		puspDao = new PackingUnsortedShapePointsDao().withVerbose(verbose);
		// Copy over
		pspDao.getShapeIds().forEach(sid -> pspDao.getPointsOfShape(sid).get()
				.forEach(sp -> puspDao.addShapePoint(sp)));
		System.out.println(
				"Copied " + puspDao.getShapePointsCount() + " shape points.");
		// Free memory
		pspDao = null;
		currentDao = puspDao;
		return true;
	}
}
