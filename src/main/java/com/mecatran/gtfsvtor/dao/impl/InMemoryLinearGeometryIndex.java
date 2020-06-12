package com.mecatran.gtfsvtor.dao.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Lists;
import com.mecatran.gtfsvtor.dao.IndexedReadOnlyDao;
import com.mecatran.gtfsvtor.dao.LinearGeometryIndex;
import com.mecatran.gtfsvtor.geospatial.GeoCoordinates;
import com.mecatran.gtfsvtor.geospatial.Geodesics;
import com.mecatran.gtfsvtor.model.GtfsShape;
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

		private double arcLengthMeters;
		private double distanceToShapeMeters;
		private GeoCoordinates projectedPoint;
		private boolean hasShape;
		private GtfsStop.Id stopId;
		private GtfsTripStopSequence stopSequence;

		private ProjectedPointImpl(double arcLengthMeters,
				double distanceToShapeMeters, GeoCoordinates projectedPoint,
				boolean hasShape, GtfsStop.Id stopId,
				GtfsTripStopSequence stopSequence) {
			this.distanceToShapeMeters = distanceToShapeMeters;
			this.arcLengthMeters = arcLengthMeters;
			this.projectedPoint = projectedPoint;
			this.hasShape = hasShape;
			this.stopId = stopId;
			this.stopSequence = stopSequence;
		}

		@Override
		public double getArcLengthMeters() {
			return arcLengthMeters;
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
		public GtfsStop.Id getStopId() {
			return stopId;
		}

		@Override
		public GtfsTripStopSequence getStopSequence() {
			return stopSequence;
		}

		@Override
		public String toString() {
			return String.format("ProjectedPoint{t=%.2fm,d=%.2fm,pp=%s,s=%b}",
					arcLengthMeters, distanceToShapeMeters, projectedPoint,
					hasShape);
		}
	}

	private static class ProjectedShapePatternImpl
			implements ProjectedShapePattern {
		private GtfsShape.Id shapeId;
		private SortedMap<GtfsTripStopSequence, ProjectedPointImpl> projections = new TreeMap<>();
		private Set<GtfsTrip.Id> tripIds = new HashSet<>();

		private ProjectedShapePatternImpl(GtfsShape.Id shapeId) {
			this.shapeId = shapeId;
		}

		@Override
		public List<? extends ProjectedPoint> getProjectedPoints() {
			return new ArrayList<>(projections.values());
		}

		@Override
		public Stream<GtfsTrip.Id> getTripIds() {
			return tripIds.stream();
		}

		@Override
		public Optional<GtfsShape.Id> getShapeId() {
			return Optional.ofNullable(shapeId);
		}
	}

	private Map<GtfsTrip.Id, ProjectedShapePatternImpl> patternIndexByTrips = new HashMap<>();
	private List<ProjectedShapePatternImpl> patternIndexes = new ArrayList<>();
	private int nPatterns = 0;

	public InMemoryLinearGeometryIndex(IndexedReadOnlyDao dao) {
		Map<Object, ProjectedShapePatternImpl> patternIndexesByPattern = new HashMap<>();
		dao.getTripsAndTimes().forEach(tripTimes -> {
			GtfsTrip trip = tripTimes.getTrip();
			List<GtfsStopTime> stopTimes = tripTimes.getStopTimes();
			Object tripKey = computeTripKey(trip, stopTimes);
			ProjectedShapePatternImpl patternIndex = patternIndexesByPattern
					.get(tripKey);
			if (patternIndex == null) {
				patternIndex = computePatternIndex(trip, stopTimes, dao);
				patternIndexesByPattern.put(tripKey, patternIndex);
				patternIndexes.add(patternIndex);
				nPatterns++;
			}
			patternIndexByTrips.put(trip.getId(), patternIndex);
			patternIndex.tripIds.add(trip.getId());
		});
	}

	int getPatternCount() {
		return nPatterns;
	}

	@Override
	public ProjectedPoint getProjectedPoint(GtfsStopTime stopTime) {
		ProjectedShapePatternImpl linearIndex = patternIndexByTrips
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
		ProjectedShapePatternImpl linearIndex = patternIndexByTrips
				.get(stopTime1.getTripId());
		if (linearIndex == null)
			return null;
		ProjectedPointImpl ppos1 = linearIndex.projections
				.get(stopTime1.getStopSequence());
		ProjectedPointImpl ppos2 = linearIndex.projections
				.get(stopTime2.getStopSequence());
		if (ppos1 == null || ppos2 == null)
			return null;
		Double d1 = ppos1.arcLengthMeters;
		Double d2 = ppos2.arcLengthMeters;
		if (d1 == null || d2 == null)
			return null;
		return d2 - d1;
	}

	@Override
	public Stream<? extends ProjectedShapePattern> getProjectedPatterns() {
		return patternIndexes.stream();
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

	private ProjectedShapePatternImpl computePatternIndex(GtfsTrip trip,
			List<GtfsStopTime> stopTimes, IndexedReadOnlyDao dao) {
		List<GtfsShapePoint> shapePoints = dao
				.getPointsOfShape(trip.getShapeId());

		ProjectedShapePatternImpl ret;
		if (trip.getShapeId() == null || shapePoints.size() < 2) {
			// No shape, linear index on inter-stop distance
			ret = computeShapelessPatternIndex(trip, stopTimes, dao);
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
				ret = computeShapedWithDistPatternIndex(trip, stopTimes,
						shapePoints, dao);
			} else {
				// shape_dist_traveled is not present
				ret = computeShapedWithoutDistPatternIndex(trip, stopTimes,
						shapePoints, dao);
			}
		}
		if (ret == null) {
			// Can happen in case shape is bogus (no coordinates)
			// Fallback on simple case
			ret = computeShapelessPatternIndex(trip, stopTimes, dao);
		}
		return ret;
	}

	private ProjectedShapePatternImpl computeShapelessPatternIndex(
			GtfsTrip trip, List<GtfsStopTime> stopTimes,
			IndexedReadOnlyDao dao) {
		ProjectedShapePatternImpl patternIndex = new ProjectedShapePatternImpl(
				null);
		GeoCoordinates lastGeocodedPoint = null;
		double totalDistance = 0.0;
		for (GtfsStopTime stopTime : stopTimes) {
			GtfsStop stop = dao.getStop(stopTime.getStopId());
			if (stop == null)
				continue;
			Optional<GeoCoordinates> p = stop.getValidCoordinates();
			if (!p.isPresent()) {
				continue;
			}
			if (lastGeocodedPoint != null) {
				double distance = Geodesics.distanceMeters(lastGeocodedPoint,
						p.get());
				totalDistance += distance;
			}
			/*
			 * In this simple case the distance to "shape" is always 0 and the
			 * projected point is the stop itself.
			 */
			patternIndex.projections.put(stopTime.getStopSequence(),
					new ProjectedPointImpl(totalDistance, 0.0, p.get(), false,
							stopTime.getStopId(), stopTime.getStopSequence()));
			lastGeocodedPoint = p.get();
		}
		return patternIndex;
	}

	private ProjectedShapePatternImpl computeShapedWithDistPatternIndex(
			GtfsTrip trip, List<GtfsStopTime> stopTimes,
			List<GtfsShapePoint> shapePoints, IndexedReadOnlyDao dao) {
		ProjectedShapePatternImpl patternIndex = new ProjectedShapePatternImpl(
				shapePoints.isEmpty() ? null : shapePoints.get(0).getShapeId());

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
				double dap = stop == null || ! stop.getValidCoordinates().isPresent() ? 0.
						: Geodesics.distanceMeters(stop.getValidCoordinates().get(),
								a.getCoordinates());
				patternIndex.projections.put(st.getStopSequence(),
						new ProjectedPointImpl(distance, dap,
								a.getCoordinates(), true, st.getStopId(),
								st.getStopSequence()));
				stopIndex++;
			} else if (kp <= kb) {
				// Stop is within the segment [a-b], interpolate
				// k is unit factor in the range [0..1] between A and B
				// k=0 - we are at A, k=1 - we are at B
				double k = Math.abs(kb - ka) < 1e-10 ? 0.
						: (kp - ka) / (kb - ka);
				double dap = dab * k;
				GeoCoordinates pa = a.getCoordinates();
				GeoCoordinates pb = b.getCoordinates();
				GeoCoordinates pp = new GeoCoordinates(
						pa.getLat() + (pb.getLat() - pa.getLat()) * k,
						pa.getLon() + (pb.getLon() - pa.getLon()) * k);
				// Compute distance from stop to shape, if possible
				double dpp = stop == null || ! stop.getValidCoordinates().isPresent()? 0.0
						: Geodesics.distanceMeters(stop.getValidCoordinates().get(), pp);
				patternIndex.projections.put(st.getStopSequence(),
						new ProjectedPointImpl(distance + dap, dpp, pp, true,
								st.getStopId(), st.getStopSequence()));
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
			double dbp = stop == null || ! stop.getValidCoordinates().isPresent() ? 0.
					: Geodesics.distanceMeters(stop.getValidCoordinates().get(),
							pb.getCoordinates());
			patternIndex.projections.put(st.getStopSequence(),
					new ProjectedPointImpl(distance, dbp, pb.getCoordinates(),
							true, st.getStopId(), st.getStopSequence()));
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

	private ProjectedShapePatternImpl computeShapedWithoutDistPatternIndex(
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
		 * This is the tricky part. Here we add virtual local minimum if needed,
		 * to make sure we find a solution for corner cases.
		 */
		LocalMin currentMax = start;
		for (int i = 0; i < stopTimes.size(); i++) {
			GtfsStopTime stopTime = stopTimes.get(i);
			GtfsStop stop = dao.getStop(stopTime.getStopId());
			List<LocalMin> localMinsForStop = localMinsPerStop.get(i + 1);
			LocalMin maxForStop = localMinsForStop
					.get(localMinsForStop.size() - 1);
			if (maxForStop.compareTo(currentMax) < 0) {
				// Add extra local min after maxForStop
				double d2 = Geodesics.distanceMeters(currentMax.projectedPoint,
						stop.getCoordinates());
				LocalMin extraLocalMin = new LocalMin(i,
						currentMax.segmentIndex, currentMax.kSegment, d2,
						currentMax.projectedPoint);
				if (_debug)
					System.out.println("---" + extraLocalMin);
				localMinsForStop.add(0, extraLocalMin);
				// currentMax do not change
			} else {
				// increase currentMax
				currentMax = maxForStop;
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

		if (_debug && path != null) {
			System.out.println("=== Shortest path through local min graph ===");
			path.forEach(lm -> System.out.println(lm));
			System.out.println("===");
		}

		if (path == null || path.size() != stopTimes.size() + 2) {
			throw new AssertionError("Cannot compute path for shape "
					+ shapePoints.get(0).getShapeId() + " / trip "
					+ trip.getId() + ": "
					+ (path == null ? "no path"
							: "invalid path size " + path.size() + "!="
									+ (stopTimes.size() + 2)));
		}

		ProjectedShapePatternImpl pattern = new ProjectedShapePatternImpl(
				shapePoints.isEmpty() ? null : shapePoints.get(0).getShapeId());

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
							min.projectedPoint, true, stopTime.getStopId(),
							stopTime.getStopSequence()));
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
