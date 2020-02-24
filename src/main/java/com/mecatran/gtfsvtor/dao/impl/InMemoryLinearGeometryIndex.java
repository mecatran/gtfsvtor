package com.mecatran.gtfsvtor.dao.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import com.mecatran.gtfsvtor.dao.IndexedReadOnlyDao;
import com.mecatran.gtfsvtor.dao.LinearGeometryIndex;
import com.mecatran.gtfsvtor.geospatial.GeoCoordinates;
import com.mecatran.gtfsvtor.geospatial.Geodesics;
import com.mecatran.gtfsvtor.model.GtfsShapePoint;
import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.model.GtfsStopTime;
import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.model.GtfsTripStopSequence;
import com.mecatran.gtfsvtor.utils.AStar;
import com.mecatran.gtfsvtor.utils.PathFinder;
import com.mecatran.gtfsvtor.utils.PathFinder.Graph;
import com.mecatran.gtfsvtor.utils.PathFinder.TraverseInfo;

public class InMemoryLinearGeometryIndex implements LinearGeometryIndex {

	private static class ProjectedPointImpl implements ProjectedPoint {

		private double linearDistanceMeters;
		private double distanceToShapeMeters;
		private GeoCoordinates projectedPoint;
		private boolean hasShape;

		private ProjectedPointImpl(double linarDistanceMeters,
				double distanceToShapeMeters, GeoCoordinates projectedPoint,
				boolean hasShape) {
			this.distanceToShapeMeters = distanceToShapeMeters;
			this.linearDistanceMeters = linarDistanceMeters;
			this.projectedPoint = projectedPoint;
			this.hasShape = hasShape;
		}

		@Override
		public double getLinearDistanceMeters() {
			return linearDistanceMeters;
		}

		@Override
		public double getDistanceToShapeMeters() {
			return distanceToShapeMeters;
		}

		@Override
		public GeoCoordinates getProjectedPoint() {
			return projectedPoint;
		}

		@Override
		public boolean hasShape() {
			return hasShape;
		}

		@Override
		public String toString() {
			return String.format("ProjectedPoint{t=%.2fm,d=%.2fm,pp=%s,s=%b}",
					linearDistanceMeters, distanceToShapeMeters, projectedPoint,
					hasShape);
		}
	}

	private static class PatternLinearIndex {
		private Map<GtfsTripStopSequence, ProjectedPointImpl> projections = new HashMap<>();
	}

	private Map<GtfsTrip.Id, PatternLinearIndex> patternIndexByTrips = new HashMap<>();
	private int nPatterns = 0;

	public InMemoryLinearGeometryIndex(IndexedReadOnlyDao dao) {
		Map<Object, PatternLinearIndex> patternIndexes = new HashMap<>();
		for (GtfsTrip trip : dao.getTrips()) {
			List<GtfsStopTime> stopTimes = dao.getStopTimesOfTrip(trip.getId());
			Object tripKey = computeTripKey(trip, stopTimes);
			PatternLinearIndex patternIndex = patternIndexes.get(tripKey);
			if (patternIndex == null) {
				patternIndex = computePatternIndex(trip, stopTimes, dao);
				patternIndexes.put(tripKey, patternIndex);
				nPatterns++;
			}
			patternIndexByTrips.put(trip.getId(), patternIndex);
		}
	}

	int getPatternCount() {
		return nPatterns;
	}

	@Override
	public ProjectedPoint getProjectedPoint(GtfsStopTime stopTime) {
		PatternLinearIndex linearIndex = patternIndexByTrips
				.get(stopTime.getTripId());
		if (linearIndex == null)
			return null;
		ProjectedPointImpl ppos = linearIndex.projections
				.get(stopTime.getStopSequence());
		return ppos;
	}

	@Override
	public Double getLinearDistance(GtfsStopTime stopTime1,
			GtfsStopTime stopTime2) {
		if (!stopTime1.getTripId().equals(stopTime2.getTripId()))
			throw new IllegalArgumentException(
					"Cannot get linear distance between stop times of different trips! Trip IDs: "
							+ stopTime1.getTripId() + " vs "
							+ stopTime2.getTripId());
		PatternLinearIndex linearIndex = patternIndexByTrips
				.get(stopTime1.getTripId());
		if (linearIndex == null)
			return null;
		ProjectedPointImpl ppos1 = linearIndex.projections
				.get(stopTime1.getStopSequence());
		ProjectedPointImpl ppos2 = linearIndex.projections
				.get(stopTime2.getStopSequence());
		if (ppos1 == null || ppos2 == null)
			return null;
		Double d1 = ppos1.linearDistanceMeters;
		Double d2 = ppos2.linearDistanceMeters;
		if (d1 == null || d2 == null)
			return null;
		return d2 - d1;
	}

	/*
	 * Compute a key that guarantee to return the same linear index for each
	 * element that map to the same key. Here we include in the key the shape ID
	 * (or null), the ordered list of pairs (stop IDs, shape dist traveled).
	 */
	private Object computeTripKey(GtfsTrip trip, List<GtfsStopTime> stopTimes) {
		List<Object> tripKey = new ArrayList<>(stopTimes.size() + 1);
		// First key in list is shape ID, can be null
		tripKey.add(trip.getShapeId());
		// All other keys are stop IDs + shape dist traveled, in order
		for (GtfsStopTime stopTime : stopTimes) {
			if (stopTime.getStopId() == null)
				continue; // Skip
			tripKey.add(stopTime.getStopId());
			// The field below can be null
			tripKey.add(stopTime.getShapeDistTraveled());
		}
		return tripKey;
	}

	private PatternLinearIndex computePatternIndex(GtfsTrip trip,
			List<GtfsStopTime> stopTimes, IndexedReadOnlyDao dao) {
		List<GtfsShapePoint> shapePoints = dao
				.getPointsOfShape(trip.getShapeId());

		if (trip.getShapeId() == null || shapePoints.size() < 2) {
			// No shape, linear index on inter-stop distance
			return computeShapelessPatternIndex(trip, stopTimes, dao);
		} else {
			// A shape is present, check shape_dist_traveled
			boolean shapeDistTraveled = true;
			for (GtfsShapePoint shapePoint : shapePoints) {
				if (shapePoint.getShapeDistTraveled() == null) {
					shapeDistTraveled = false;
					break;
				}
			}
			if (shapeDistTraveled) {
				for (GtfsStopTime stopTime : stopTimes) {
					if (stopTime.getShapeDistTraveled() == null) {
						shapeDistTraveled = false;
						break;
					}
				}
			}
			if (shapeDistTraveled) {
				// shape_dist_traveled is present for both shape and stop times
				return computeShapedWithDistPatternIndex(trip, stopTimes,
						shapePoints, dao);
			} else {
				// shape_dist_traveled is not present
				return computeShapedWithoutDistPatternIndex(trip, stopTimes,
						shapePoints, dao);
			}
		}
	}

	private PatternLinearIndex computeShapelessPatternIndex(GtfsTrip trip,
			List<GtfsStopTime> stopTimes, IndexedReadOnlyDao dao) {
		PatternLinearIndex patternIndex = new PatternLinearIndex();
		GeoCoordinates lastGeocodedPoint = null;
		double totalDistance = 0.0;
		for (GtfsStopTime stopTime : stopTimes) {
			GtfsStop stop = dao.getStop(stopTime.getStopId());
			if (stop == null)
				continue;
			GeoCoordinates p = stop.getCoordinates();
			if (p == null)
				continue;
			if (lastGeocodedPoint != null) {
				double distance = Geodesics.distanceMeters(lastGeocodedPoint,
						p);
				totalDistance += distance;
			}
			/*
			 * In this simple case the distance to "shape" is always 0 and the
			 * projected point is the stop itself.
			 */
			patternIndex.projections.put(stopTime.getStopSequence(),
					new ProjectedPointImpl(totalDistance, 0.0, p, false));
			lastGeocodedPoint = p;
		}
		return patternIndex;
	}

	private PatternLinearIndex computeShapedWithDistPatternIndex(GtfsTrip trip,
			List<GtfsStopTime> stopTimes, List<GtfsShapePoint> shapePoints,
			IndexedReadOnlyDao dao) {
		PatternLinearIndex patternIndex = new PatternLinearIndex();

		int stopIndex = 0;
		int segmentIndex = 0;
		double distance = 0.0;
		Double dab = null;

		while (stopIndex < stopTimes.size()
				&& segmentIndex < shapePoints.size() - 1) {
			GtfsShapePoint a = shapePoints.get(segmentIndex);
			GtfsShapePoint b = shapePoints.get(segmentIndex + 1);
			GtfsStopTime st = stopTimes.get(stopIndex);
			GtfsStop stop = dao.getStop(st.getStopId());
			double ka = a.getShapeDistTraveled();
			double kb = b.getShapeDistTraveled();
			double kp = st.getShapeDistTraveled();
			if (dab == null) {
				dab = Geodesics.distanceMeters(a.getCoordinates(),
						b.getCoordinates());
			}
			if (kp < ka) {
				// This is wrong
				// Stops before the start of shape
				double dap = stop == null ? 0.
						: Geodesics.distanceMeters(stop.getCoordinates(),
								a.getCoordinates());
				patternIndex.projections.put(st.getStopSequence(),
						new ProjectedPointImpl(distance, dap,
								a.getCoordinates(), true));
				stopIndex++;
			} else if (kp <= kb) {
				// Stop is within the segment [a-b], interpolate
				// k is unit factor in the range [0..1] between A and B
				// k=0 - we are at A, k=1 - we are at B
				double k = Math.abs(kb - ka) < 1e-10 ? 0.
						: (kp - ka) / (kb - ka);
				double dap = dab * k;
				GeoCoordinates pp = new GeoCoordinates(
						a.getLat() + (b.getLat() - a.getLat()) * k,
						a.getLon() + (b.getLon() - a.getLon()) * k);
				// Compute distance from stop to shape, if possible
				double dpp = stop == null ? 0.0
						: Geodesics.distanceMeters(stop.getCoordinates(), pp);
				patternIndex.projections.put(st.getStopSequence(),
						new ProjectedPointImpl(distance + dap, dpp, pp, true));
				stopIndex++;
			} else {
				// Go to next segment
				segmentIndex++;
				distance += dab;
				dab = null;
			}
		}

		GtfsShapePoint pb = shapePoints.get(shapePoints.size() - 1);
		for (; stopIndex < stopTimes.size(); stopIndex++) {
			// If we are here, this is also wrong
			// Stops after the end of the shape
			GtfsStopTime st = stopTimes.get(stopIndex);
			GtfsStop stop = dao.getStop(st.getStopId());
			double dbp = stop == null ? 0.
					: Geodesics.distanceMeters(stop.getCoordinates(),
							pb.getCoordinates());
			patternIndex.projections.put(st.getStopSequence(),
					new ProjectedPointImpl(distance, dbp, pb.getCoordinates(),
							true));
		}

		return patternIndex;
	}

	private static class LocalMin implements Comparable<LocalMin> {
		private int stopIndex;
		private int segmentIndex;
		private double kSegment;
		private double distanceToProjectedPointMeters;
		private GeoCoordinates projectedPoint;

		private LocalMin(int stopIndex, int segmentIndex, double kSegment,
				double distanceToProjectedPointMeters,
				GeoCoordinates projectedPoint) {
			this.stopIndex = stopIndex;
			this.segmentIndex = segmentIndex;
			this.kSegment = kSegment;
			this.distanceToProjectedPointMeters = distanceToProjectedPointMeters;
			this.projectedPoint = projectedPoint;
		}

		@Override
		public String toString() {
			return String.format(Locale.US,
					"LocalMinimum{iStop=%d,iSeg=%d,kSeg=%.6f,dpp=%.3fm,pp=%s}",
					stopIndex, segmentIndex, kSegment,
					distanceToProjectedPointMeters, projectedPoint);
		}

		@Override
		public int compareTo(LocalMin o) {
			int cmp = Integer.compare(segmentIndex, o.segmentIndex);
			if (cmp != 0)
				return cmp;
			return Double.compare(kSegment, o.kSegment);
		}
	}

	private boolean _debug = false;

	private PatternLinearIndex computeShapedWithoutDistPatternIndex(
			GtfsTrip trip, List<GtfsStopTime> stopTimes,
			List<GtfsShapePoint> shapePoints, IndexedReadOnlyDao dao) {

		if (_debug)
			System.out.println(
					"=== Index shape " + shapePoints.get(0).getShapeId()
							+ " trip " + trip.getId());

		// Compute cache of segment linear distance from start
		List<Double> segmentLinearDistanceMeters = new ArrayList<>();
		double linearDistanceMeters = 0;
		for (int i = 0; i < shapePoints.size() - 1; i++) {
			GeoCoordinates pa = shapePoints.get(i).getCoordinates();
			GeoCoordinates pb = shapePoints.get(i + 1).getCoordinates();
			if (pa == null || pb == null)
				return null;
			segmentLinearDistanceMeters.add(linearDistanceMeters);
			linearDistanceMeters += Geodesics.fastDistanceMeters(pa, pb);
		}

		// Compute all local minima from each point to the shape
		List<List<LocalMin>> localMinsPerStop = new ArrayList<>();
		LocalMin start = new LocalMin(-1, 0, 0.0, 0.0, null);
		localMinsPerStop.add(Arrays.asList(start));
		for (int i = 0; i < stopTimes.size(); i++) {
			GtfsStopTime stopTime = stopTimes.get(i);
			GtfsStop stop = dao.getStop(stopTime.getStopId());
			List<LocalMin> localMinsForStop = computeLocalMins(i, stop,
					shapePoints);
			if (_debug) {
				System.out.println("Local mins for " + stop);
				localMinsForStop.forEach(lm -> System.out.println("   " + lm));
			}
			localMinsPerStop.add(localMinsForStop);
		}
		LocalMin goal = new LocalMin(stopTimes.size(), Integer.MAX_VALUE, 1.0,
				0.0, null);
		localMinsPerStop.add(Arrays.asList(goal));

		/*
		 * This is the tricky part. Here we add virtual local minimum to the
		 * local min of the previous stop, if needed, in order to be able to
		 * backtrack in case we did not detected the correct "optimal" local
		 * minimum.
		 */
		LocalMin currentMax = goal;
		for (int i = stopTimes.size() - 1; i >= 0; i--) {
			GtfsStopTime stopTime = stopTimes.get(i);
			GtfsStop stop = dao.getStop(stopTime.getStopId());
			List<LocalMin> localMinsForStop = localMinsPerStop.get(i + 1);
			LocalMin minForStop = localMinsForStop.get(0);
			if (minForStop.compareTo(currentMax) > 0) {
				double d2 = Geodesics.distanceMeters(currentMax.projectedPoint,
						stop.getCoordinates());
				LocalMin extraLocalMin = new LocalMin(i,
						currentMax.segmentIndex, currentMax.kSegment, d2,
						currentMax.projectedPoint);
				if (_debug)
					System.out.println("---" + extraLocalMin);
				localMinsForStop.add(0, extraLocalMin);
			} else {
				currentMax = localMinsForStop.get(localMinsForStop.size() - 1);
			}
		}

		// 2. Compute shortest path in graph
		PathFinder<LocalMin> pathFinder = new AStar<>();
		List<LocalMin> path = pathFinder.findPath(new Graph<LocalMin>() {
			@Override
			public double pathCostEstimate(LocalMin node, LocalMin goal) {
				// Fallback on Dijkstra for now
				return 0; // TODO - estimate sum(min(d^2))
			}

			@Override
			public Iterable<TraverseInfo<LocalMin>> neighbors(LocalMin node) {
				double cost = node.distanceToProjectedPointMeters
						* node.distanceToProjectedPointMeters;
				return localMinsPerStop.get(node.stopIndex + 2).stream()
						.filter(lm -> lm.compareTo(node) >= 0)
						.map(lm -> new TraverseInfo<>(lm, cost))
						.collect(Collectors.toList());
			}
		}, start, goal);
		// Path contains start and end
		assert (path != null);
		assert (path.size() == stopTimes.size() + 2);

		if (_debug) {
			System.out.println("=== Shortest path through local min graph ===");
			path.forEach(lm -> System.out.println(lm));
			System.out.println("===");
		}

		PatternLinearIndex pattern = new PatternLinearIndex();
		for (int i = 0; i < stopTimes.size(); i++) {
			GtfsStopTime stopTime = stopTimes.get(i);
			LocalMin min = path.get(i + 1);
			double segLen = Geodesics.fastDistanceMeters(
					shapePoints.get(min.segmentIndex).getCoordinates(),
					shapePoints.get(min.segmentIndex + 1).getCoordinates());
			double segDst = segmentLinearDistanceMeters.get(min.segmentIndex);
			pattern.projections.put(stopTime.getStopSequence(),
					new ProjectedPointImpl(segDst + segLen * min.kSegment,
							min.distanceToProjectedPointMeters,
							min.projectedPoint, true));
		}
		return pattern;
	}

	// A large value means we are more conservative but slower
	private static double MIN_THRESHOLD_METERS = 50;
	// A small value means we are more conservative but slower
	private static double MAX_THRESHOLD_METERS = 50;

	/**
	 * Compute a list of local minimum from a stop to a shape. Using a spatial
	 * index to speed things up is probably not faster than using a brute-force
	 * approach of computing the distance to every segment of the shape, since
	 * we are using a fast version of the distance function.
	 */
	private List<LocalMin> computeLocalMins(int stopIndex, GtfsStop stop,
			List<GtfsShapePoint> shapePoints) {
		List<LocalMin> ret = new ArrayList<>();
		LocalMin min = null;
		double bestMinDist = Double.MAX_VALUE;
		double minDist = Double.MAX_VALUE;
		GeoCoordinates p = stop.getCoordinates();
		double cosLat = Math.cos(Math.toRadians(p.getLat()));
		for (int segIndex = 0; segIndex < shapePoints.size() - 1; segIndex++) {
			GtfsShapePoint a = shapePoints.get(segIndex);
			GtfsShapePoint b = shapePoints.get(segIndex + 1);
			GeoCoordinates pa = a.getCoordinates();
			GeoCoordinates pb = b.getCoordinates();
			// Compute distance from stop to shape segment
			double[] dk = Geodesics.fastDistanceMeters(p, a.getCoordinates(),
					b.getCoordinates(), cosLat);
			double d = dk[0];
			double k = dk[1];
			double dpb = Geodesics.fastDistanceMeters(p, pb, cosLat);
			if (d < minDist && d < bestMinDist + MIN_THRESHOLD_METERS) {
				// Found new potential local minimum
				minDist = d;
				if (minDist < bestMinDist) {
					bestMinDist = minDist;
				}
				GeoCoordinates pp = new GeoCoordinates(
						pa.getLat() + (pb.getLat() - pa.getLat()) * k,
						pa.getLon() + (pb.getLon() - pa.getLon()) * k);
				min = new LocalMin(stopIndex, segIndex, k, d, pp);
			}
			if (dpb > minDist + MAX_THRESHOLD_METERS) {
				// We've gone too far
				if (min != null) {
					// Add new local minimum
					ret.add(min);
					min = null;
				}
				minDist = Double.MAX_VALUE;
			}
		}
		// Add last local minimum if any
		if (min != null) {
			ret.add(min);
		}
		final double threshold = bestMinDist + MIN_THRESHOLD_METERS;
		return ret.stream()
				.filter(lm -> lm.distanceToProjectedPointMeters < threshold)
				.collect(Collectors.toList());
	}

}
