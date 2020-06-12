package com.mecatran.gtfsvtor.validation.triptimes;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.mecatran.gtfsvtor.model.GtfsLogicalTime;
import com.mecatran.gtfsvtor.model.GtfsStopTime;
import com.mecatran.gtfsvtor.model.GtfsTripAndTimes;
import com.mecatran.gtfsvtor.validation.DaoValidator.Context;
import com.mecatran.gtfsvtor.validation.DefaultDisabledValidator;
import com.mecatran.gtfsvtor.validation.TripTimesValidator;

@DefaultDisabledValidator
public class StopTimesStatsValidator implements TripTimesValidator {

	private static class DeltaStats {
		private Map<Integer, AtomicInteger> deltaCount = new HashMap<>();

		private void stat(int delta) {
			deltaCount.computeIfAbsent(delta, d -> new AtomicInteger())
					.addAndGet(1);
		}

		private void print(PrintStream ps) {
			deltaCount.entrySet().stream().sorted(
					(e1, e2) -> Integer.compare(e1.getKey(), e2.getKey()))
					.forEach(e -> ps.println(String.format("%5ds - %d",
							e.getKey(), e.getValue().get())));
		}
	}

	private DeltaStats ds1, ds2;

	@Override
	public void start(Context context) {
		ds1 = new DeltaStats();
		ds2 = new DeltaStats();
	}

	@Override
	public void validate(Context context, GtfsTripAndTimes tripAndTimes) {
		List<GtfsStopTime> stopTimes = tripAndTimes.getStopTimes();

		GtfsStopTime lastValidStopTime = null;
		for (GtfsStopTime stopTime : stopTimes) {
			GtfsLogicalTime departureTime = stopTime
					.getDepartureOrArrivalTime();
			GtfsLogicalTime arrivalTime = stopTime.getArrivalOrDepartureTime();
			if (arrivalTime != null) {
				if (lastValidStopTime != null) {
					int deltaSec = arrivalTime.getSecondSinceMidnight()
							- lastValidStopTime.getDepartureOrArrivalTime()
									.getSecondSinceMidnight();
					ds1.stat(deltaSec);
				}
				lastValidStopTime = stopTime;
				if (departureTime != null) {
					int deltaSec2 = departureTime.getSecondSinceMidnight()
							- arrivalTime.getSecondSinceMidnight();
					ds2.stat(deltaSec2);
				}
			}
		}
	}

	@Override
	public void end(Context context) {
		System.out.println("Departure -> Arrival sec delays histogram:");
		ds1.print(System.out);
		System.out.println("Arrival -> Departure sec delays histogram:");
		ds2.print(System.out);
	}
}
