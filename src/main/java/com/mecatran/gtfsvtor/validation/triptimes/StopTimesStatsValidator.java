package com.mecatran.gtfsvtor.validation.triptimes;

import java.util.List;

import com.mecatran.gtfsvtor.model.GtfsLogicalTime;
import com.mecatran.gtfsvtor.model.GtfsStopTime;
import com.mecatran.gtfsvtor.model.GtfsTripAndTimes;
import com.mecatran.gtfsvtor.utils.Histogram;
import com.mecatran.gtfsvtor.validation.DaoValidator.Context;
import com.mecatran.gtfsvtor.validation.DefaultDisabledValidator;
import com.mecatran.gtfsvtor.validation.TripTimesValidator;

@DefaultDisabledValidator
public class StopTimesStatsValidator implements TripTimesValidator {

	private Histogram<Integer> hop, slack;

	@Override
	public void start(Context context) {
		hop = new Histogram<Integer>("Hop times");
		slack = new Histogram<Integer>("Slack times");
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
					int hopSec = arrivalTime.getSecondSinceMidnight()
							- lastValidStopTime.getDepartureOrArrivalTime()
									.getSecondSinceMidnight();
					hop.count(hopSec);
				}
				lastValidStopTime = stopTime;
				if (departureTime != null) {
					int slackSec = departureTime.getSecondSinceMidnight()
							- arrivalTime.getSecondSinceMidnight();
					slack.count(slackSec);
				}
			}
		}
	}

	@Override
	public void end(Context context) {
		System.out.println("Departure -> Arrival sec delays histogram:");
		System.out.println(hop);
		System.out.println("Arrival -> Departure sec delays histogram:");
		System.out.println(slack);
	}
}
