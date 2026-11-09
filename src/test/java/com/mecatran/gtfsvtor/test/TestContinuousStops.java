package com.mecatran.gtfsvtor.test;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import com.mecatran.gtfsvtor.lib.GtfsVtorOptions.StopTimesDaoMode;
import com.mecatran.gtfsvtor.model.GtfsContinuousStop;
import com.mecatran.gtfsvtor.model.GtfsRoute;
import com.mecatran.gtfsvtor.model.GtfsStopTime;
import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.SourceRefWithFields;
import com.mecatran.gtfsvtor.reporting.issues.InvalidFieldFormatError;
import com.mecatran.gtfsvtor.reporting.issues.InvalidFieldValueIssue;
import com.mecatran.gtfsvtor.reporting.issues.MissingMandatoryValueError;
import com.mecatran.gtfsvtor.test.TestUtils.TestBundle;
import com.mecatran.gtfsvtor.test.TestUtils.TestScenario;

public class TestContinuousStops {

	@Test
	public void validatesRouteDefaultsAndStopOverrides() {
		for (StopTimesDaoMode mode : StopTimesDaoMode.values()) {
			TestScenario scenario = new TestScenario("continuous_stops");
			scenario.stopTimesDaoMode = mode;
			TestBundle feed = scenario.run();
			GtfsRoute route = feed.dao.getRoute(GtfsRoute.id("R"));
			GtfsStopTime inherited = firstStop(feed, "inherited");
			assertEquals(GtfsContinuousStop.CONTINUOUS, inherited.getContinuousPickup(route));
			assertEquals(GtfsContinuousStop.COORDINATE_WITH_DRIVER, inherited.getContinuousDropoff(route));
			assertEquals(GtfsContinuousStop.NONE, firstStop(feed, "cancelled").getContinuousPickup(route));
			assertEquals(GtfsContinuousStop.PHONE_AGENCY,
					firstStop(feed, "overridden").getContinuousDropoff().get());

			assertEquals(Arrays.asList("trips.txt:2:shape_id", "trips.txt:3:shape_id"),
					locations(feed.issuesOfCategory(MissingMandatoryValueError.class)));
			assertEquals(Arrays.asList("routes.txt:2:continuous_drop_off",
					"routes.txt:2:continuous_pickup", "stop_times.txt:4:continuous_drop_off"),
					locations(feed.issuesOfCategory(InvalidFieldValueIssue.class)));
			assertEquals(Arrays.asList("routes.txt:4:continuous_pickup", "stop_times.txt:10:continuous_drop_off"),
					locations(feed.issuesOfCategory(InvalidFieldFormatError.class)));
		}
	}

	private static GtfsStopTime firstStop(TestBundle feed, String trip) {
		return feed.dao.getTripAndTimes(GtfsTrip.id(trip)).getStopTimes().get(0);
	}

	private static List<String> locations(List<? extends ReportIssue> issues) {
		return issues.stream().map(issue -> {
			SourceRefWithFields ref = issue.getSourceRefs().get(0);
			return ref.getSourceRef().getTableName() + ":" + ref.getSourceRef().getLineNumber()
					+ ":" + String.join(",", ref.getFieldNames());
		}).sorted().collect(Collectors.toList());
	}
}
