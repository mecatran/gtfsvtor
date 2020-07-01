package com.mecatran.gtfsvtor.validation.dao;

import java.util.List;

import com.mecatran.gtfsvtor.dao.IndexedReadOnlyDao;
import com.mecatran.gtfsvtor.geospatial.GeoCoordinates;
import com.mecatran.gtfsvtor.model.GtfsShapePoint;
import com.mecatran.gtfsvtor.utils.Histogram;
import com.mecatran.gtfsvtor.validation.DaoValidator;
import com.mecatran.gtfsvtor.validation.DefaultDisabledValidator;

/**
 * TODO Remove this code.
 */
@DefaultDisabledValidator
public class ShapeStatsValidator implements DaoValidator {

	private static int E7_FACTOR = 10000000;

	private boolean histValues = false;

	Histogram<Integer> latDeltaHist = new Histogram<Integer>("ΔLat");
	Histogram<Integer> lonDeltaHist = new Histogram<Integer>("ΔLon");
	Histogram<Integer> latDelta2Hist = new Histogram<Integer>("ΔLat²");
	Histogram<Integer> lonDelta2Hist = new Histogram<Integer>("ΔLon²");
	Histogram<Integer> latBytesDeltaHist = new Histogram<Integer>("latBytes");
	Histogram<Integer> lonBytesDeltaHist = new Histogram<Integer>("lonBytes");

	@Override
	public void validate(DaoValidator.Context context) {
		IndexedReadOnlyDao dao = context.getDao();

		dao.getShapeIds().forEach(shapeId -> {
			List<GtfsShapePoint> shapePoints = dao.getPointsOfShape(shapeId);

			int lastIlat = 0;
			int lastIlon = 0;
			int lastIlatDelta = 0;
			int lastIlonDelta = 0;

			for (GtfsShapePoint shapePoint : shapePoints) {
				GeoCoordinates c = shapePoint.getCoordinates();
				if (c == null)
					continue;
				int ilat = (int) (c.getLat() * E7_FACTOR);
				int ilon = (int) (c.getLon() * E7_FACTOR);
				int ilatDelta = ilat - lastIlat;
				int ilonDelta = ilon - lastIlon;
				int ilatDelta2 = ilatDelta - lastIlatDelta;
				int ilonDelta2 = ilonDelta - lastIlonDelta;

				if (histValues) {
					latDeltaHist.count(ilatDelta);
					lonDeltaHist.count(ilonDelta);
					latDelta2Hist.count(ilatDelta2);
					lonDelta2Hist.count(ilonDelta2);
				}

				latBytesDeltaHist.count(nBytes(ilatDelta, ilatDelta2));
				lonBytesDeltaHist.count(nBytes(ilonDelta, ilonDelta2));

				lastIlat = ilat;
				lastIlon = ilon;
				lastIlatDelta = ilatDelta;
				lastIlonDelta = ilonDelta;
			}
		});

		System.out.println("Number of shapes: " + dao.getShapeIds().count());
		if (histValues) {
			System.out.println(latDeltaHist);
			System.out.println(lonDeltaHist);
			System.out.println(latDelta2Hist);
			System.out.println(lonDelta2Hist);
		}
		System.out.println(latBytesDeltaHist);
		System.out.println(lonBytesDeltaHist);
	}

	// Assume the following encoding:
	// b7 - 1 bit - Delta/Delta² packing (0 or 1)
	// b6-b5 - 2 bits - Number of bytes (0, 1, 2 or 4)
	// b0-b4 - 5 bits - Value, unsigned-shifted (0..31 -> -16..15)
	// 1 byte encoding: 5+8 bits (0..8191 -> -4096..4095)
	// 2 bytes encoding: 5+8+8 bits (0..2097151 -> -1048576..1048575)
	// 4 bytes encoding: the remaining

	private int nBytes(int delta, int delta2) {
		int val = Math.abs(delta) < Math.abs(delta2) ? delta : delta2;
		if (val >= -16 && val < 16)
			return 0;
		if (val >= -4096 && val < 4096)
			return 1;
		if (val >= -1048576 && val < 1048576)
			return 2;
		return 4;
	}
}
