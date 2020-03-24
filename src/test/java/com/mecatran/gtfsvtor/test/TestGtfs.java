package com.mecatran.gtfsvtor.test;

import static com.mecatran.gtfsvtor.test.TestUtils.loadAndValidate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;

import org.junit.Test;

import com.mecatran.gtfsvtor.dao.CalendarIndex;
import com.mecatran.gtfsvtor.dao.DaoSpatialIndex;
import com.mecatran.gtfsvtor.dao.IndexedReadOnlyDao;
import com.mecatran.gtfsvtor.dao.LinearGeometryIndex;
import com.mecatran.gtfsvtor.dao.LinearGeometryIndex.ProjectedPoint;
import com.mecatran.gtfsvtor.geospatial.GeoCoordinates;
import com.mecatran.gtfsvtor.geospatial.Geodesics;
import com.mecatran.gtfsvtor.model.GtfsAgency;
import com.mecatran.gtfsvtor.model.GtfsBlockId;
import com.mecatran.gtfsvtor.model.GtfsCalendar;
import com.mecatran.gtfsvtor.model.GtfsCalendarDate;
import com.mecatran.gtfsvtor.model.GtfsCalendarDateExceptionType;
import com.mecatran.gtfsvtor.model.GtfsDropoffType;
import com.mecatran.gtfsvtor.model.GtfsExactTime;
import com.mecatran.gtfsvtor.model.GtfsFrequency;
import com.mecatran.gtfsvtor.model.GtfsId;
import com.mecatran.gtfsvtor.model.GtfsLogicalDate;
import com.mecatran.gtfsvtor.model.GtfsLogicalTime;
import com.mecatran.gtfsvtor.model.GtfsPickupType;
import com.mecatran.gtfsvtor.model.GtfsRoute;
import com.mecatran.gtfsvtor.model.GtfsShape;
import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.model.GtfsStopTime;
import com.mecatran.gtfsvtor.model.GtfsStopType;
import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.model.GtfsTripDirectionId;
import com.mecatran.gtfsvtor.model.GtfsTripStopSequence;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.SourceInfoWithFields;
import com.mecatran.gtfsvtor.reporting.issues.DuplicatedColumnError;
import com.mecatran.gtfsvtor.reporting.issues.DuplicatedObjectIdError;
import com.mecatran.gtfsvtor.reporting.issues.DuplicatedTripIssue;
import com.mecatran.gtfsvtor.reporting.issues.EmptyCalendarWarning;
import com.mecatran.gtfsvtor.reporting.issues.EmptyTableError;
import com.mecatran.gtfsvtor.reporting.issues.GeneralIOError;
import com.mecatran.gtfsvtor.reporting.issues.InvalidCharsetError;
import com.mecatran.gtfsvtor.reporting.issues.InvalidEncodingError;
import com.mecatran.gtfsvtor.reporting.issues.InvalidFieldFormatError;
import com.mecatran.gtfsvtor.reporting.issues.InvalidFieldValueError;
import com.mecatran.gtfsvtor.reporting.issues.InvalidReferenceError;
import com.mecatran.gtfsvtor.reporting.issues.MissingMandatoryColumnError;
import com.mecatran.gtfsvtor.reporting.issues.MissingMandatoryTableError;
import com.mecatran.gtfsvtor.reporting.issues.MissingMandatoryValueError;
import com.mecatran.gtfsvtor.reporting.issues.MissingObjectIdError;
import com.mecatran.gtfsvtor.reporting.issues.NonIncreasingShapeDistTraveledError;
import com.mecatran.gtfsvtor.reporting.issues.OverlappingBlockIdIssue;
import com.mecatran.gtfsvtor.reporting.issues.RouteColorContrastIssue;
import com.mecatran.gtfsvtor.reporting.issues.SimilarRouteColorWarning;
import com.mecatran.gtfsvtor.reporting.issues.StopTooFarFromParentStationIssue;
import com.mecatran.gtfsvtor.reporting.issues.StopTooFarFromShapeIssue;
import com.mecatran.gtfsvtor.reporting.issues.TimeTravelAtStopError;
import com.mecatran.gtfsvtor.reporting.issues.TimeTravelError;
import com.mecatran.gtfsvtor.reporting.issues.TooFastTravelIssue;
import com.mecatran.gtfsvtor.reporting.issues.UnknownFileInfo;
import com.mecatran.gtfsvtor.reporting.issues.UnrecognizedColumnInfo;
import com.mecatran.gtfsvtor.reporting.issues.UnusedObjectWarning;
import com.mecatran.gtfsvtor.reporting.issues.WrongStopTimeStopTypeError;
import com.mecatran.gtfsvtor.test.TestUtils.TestBundle;

public class TestGtfs {

	private void testGoodDao(IndexedReadOnlyDao dao) {
		assertEquals(1, dao.getAgencies().size());
		GtfsAgency dta = dao.getAgency(GtfsAgency.id("DTA"));
		assertNotNull(dta);
		assertEquals("Autorité de passage de démonstration", dta.getName());

		assertEquals(5, dao.getRoutes().size());
		GtfsRoute city = dao.getRoute(GtfsRoute.id("CITY"));
		assertNotNull(city);
		assertEquals("Ō", city.getShortName());
		assertEquals("Bar Circle", city.getLongName());

		assertEquals(10, dao.getStops().size());
		GtfsStop nadav = dao.getStop(GtfsStop.id("NADAV"));
		assertNotNull(nadav);
		assertEquals(GtfsStopType.STOP, nadav.getType());
		assertEquals("North Ave / D Ave N (Demo)", nadav.getName());

		GtfsStop inexistant = dao.getStop(GtfsStop.id("FOOBAR"));
		assertNull(inexistant);

		GtfsCalendar cal = dao.getCalendar(GtfsCalendar.id("FULLW"));
		assertNotNull(cal);
		assertEquals(GtfsLogicalDate.getDate(2007, 1, 1), cal.getStartDate());
		assertEquals(GtfsLogicalDate.getDate(2011, 12, 31), cal.getEndDate());
		assertTrue(cal.isMonday());
		assertTrue(cal.isTuesday());
		assertTrue(cal.isWednesday());
		assertTrue(cal.isThursday());
		assertTrue(cal.isFriday());
		assertTrue(cal.isSaturday());
		assertTrue(cal.isSunday());

		Collection<GtfsCalendarDate> datesEx = dao
				.getCalendarDates(GtfsCalendar.id("FULLW"));
		assertEquals(1, datesEx.size());
		GtfsCalendarDate dateEx = datesEx.iterator().next();
		assertEquals(GtfsLogicalDate.getDate(2007, 6, 4), dateEx.getDate());
		assertEquals(GtfsCalendarDateExceptionType.REMOVED,
				dateEx.getExceptionType());

		CalendarIndex calIndex = dao.getCalendarIndex();
		SortedSet<GtfsLogicalDate> fullwDates = calIndex
				.getCalendarApplicableDates(GtfsCalendar.id("FULLW"));
		SortedSet<GtfsLogicalDate> weDates = calIndex
				.getCalendarApplicableDates(GtfsCalendar.id("WE"));
		assertEquals(1825, fullwDates.size());
		assertEquals(521, weDates.size());
		for (GtfsLogicalDate weDate : weDates) {
			assertTrue(weDate.getDayOfTheWeek() == GtfsLogicalDate.DOW_SATURDAY
					|| weDate.getDayOfTheWeek() == GtfsLogicalDate.DOW_SUNDAY);
		}
		assertFalse(fullwDates.contains(GtfsLogicalDate.getDate(2007, 6, 4)));
		assertFalse(fullwDates.contains(GtfsLogicalDate.getDate(2006, 12, 31)));
		assertFalse(fullwDates.contains(GtfsLogicalDate.getDate(2012, 1, 1)));
		assertTrue(fullwDates.contains(GtfsLogicalDate.getDate(2007, 1, 1)));
		assertTrue(fullwDates.contains(GtfsLogicalDate.getDate(2011, 12, 31)));

		assertFalse(weDates.contains(GtfsLogicalDate.getDate(2007, 1, 5)));
		assertTrue(weDates.contains(GtfsLogicalDate.getDate(2007, 1, 6)));
		assertTrue(weDates.contains(GtfsLogicalDate.getDate(2011, 12, 31)));
		assertFalse(weDates.contains(GtfsLogicalDate.getDate(2012, 1, 1)));

		Collection<GtfsCalendar.Id> calIds;
		calIds = calIndex
				.getCalendarIdsOnDate(GtfsLogicalDate.getDate(2007, 1, 1));
		assertEquals(1, calIds.size());
		assertEquals(GtfsCalendar.id("FULLW"), calIds.iterator().next());
		calIds = calIndex
				.getCalendarIdsOnDate(GtfsLogicalDate.getDate(2007, 6, 4));
		assertEquals(0, calIds.size());
		calIds = calIndex
				.getCalendarIdsOnDate(GtfsLogicalDate.getDate(2011, 12, 31));
		assertEquals(2, calIds.size());
		assertTrue(calIds.contains(GtfsCalendar.id("WE")));
		assertTrue(calIds.contains(GtfsCalendar.id("FULLW")));
		calIds = calIndex
				.getCalendarIdsOnDate(GtfsLogicalDate.getDate(2012, 1, 1));
		assertEquals(0, calIds.size());

		Collection<GtfsTrip> trips = dao.getTrips();
		assertEquals(11, trips.size());
		GtfsTrip ab1 = dao.getTrip(GtfsTrip.id("AB1"));
		assertNotNull(ab1);
		assertEquals(GtfsTrip.id("AB1"), ab1.getId());
		assertEquals(GtfsRoute.id("AB"), ab1.getRouteId());
		assertEquals(GtfsCalendar.id("FULLW"), ab1.getServiceId());
		assertEquals("to Bullfrog", ab1.getHeadsign());
		assertEquals(GtfsTripDirectionId.DIRECTION0, ab1.getDirectionId());
		assertEquals(GtfsBlockId.fromValue("1"), ab1.getBlockId());

		GtfsTrip aamv1 = dao.getTrip(GtfsTrip.id("AAMV1"));
		assertNotNull(aamv1);
		assertEquals(GtfsRoute.id("AAMV"), aamv1.getRouteId());
		assertEquals(null, aamv1.getBlockId());

		GtfsTrip unknownTrip = dao.getTrip(GtfsTrip.id("FOOBAR"));
		assertNull(unknownTrip);

		List<GtfsStopTime> city1stopTimes = dao
				.getStopTimesOfTrip(GtfsTrip.id("CITY1"));
		assertEquals(5, city1stopTimes.size());
		assertEquals(GtfsStop.id("STAGECOACH"),
				city1stopTimes.get(0).getStopId());
		assertEquals(GtfsTrip.id("CITY1"), city1stopTimes.get(0).getTripId());
		assertEquals(GtfsStop.id("EMSI"), city1stopTimes.get(4).getStopId());
		assertEquals(GtfsTrip.id("CITY1"), city1stopTimes.get(4).getTripId());
		assertEquals(GtfsLogicalTime.getTime(6, 0, 0),
				city1stopTimes.get(0).getDepartureTime());
		assertEquals(GtfsLogicalTime.getTime(6, 26, 0),
				city1stopTimes.get(4).getArrivalTime());
		assertEquals(GtfsLogicalTime.getTime(6, 28, 0),
				city1stopTimes.get(4).getDepartureTime());
		assertEquals(GtfsTripStopSequence.fromSequence(0),
				city1stopTimes.get(0).getStopSequence());
		assertEquals(GtfsTripStopSequence.fromSequence(20),
				city1stopTimes.get(4).getStopSequence());
		assertNull(city1stopTimes.get(0).getStopHeadsign());
		assertEquals("going to nadav", city1stopTimes.get(1).getStopHeadsign());
		assertEquals((double) 0.0,
				(double) city1stopTimes.get(0).getShapeDistTraveled(), 0.0);
		assertEquals((double) 4.0,
				(double) city1stopTimes.get(4).getShapeDistTraveled(), 0.0);
		assertFalse(city1stopTimes.get(0).getPickupType().isPresent());
		assertFalse(city1stopTimes.get(0).getDropoffType().isPresent());
		assertEquals(GtfsPickupType.DEFAULT_PICKUP,
				city1stopTimes.get(0).getNonNullPickupType());
		assertEquals(GtfsDropoffType.DEFAULT_DROPOFF,
				city1stopTimes.get(0).getNonNullDropoffType());

		List<GtfsStopTime> unknownStopTimes = dao
				.getStopTimesOfTrip(GtfsTrip.id("FOOBAR"));
		assertTrue(unknownStopTimes.isEmpty());

		Collection<GtfsFrequency> frequencies = dao.getFrequencies();
		assertEquals(11, frequencies.size());
		frequencies = dao.getFrequenciesOfTrip(GtfsTrip.id("STBA"));
		assertEquals(1, frequencies.size());
		GtfsFrequency frequency = frequencies.iterator().next();
		assertEquals(GtfsTrip.id("STBA"), frequency.getTripId());
		assertEquals(GtfsLogicalTime.getTime(6, 0, 0),
				frequency.getStartTime());
		assertEquals(GtfsLogicalTime.getTime(22, 0, 0), frequency.getEndTime());
		assertEquals(new Integer(1800), frequency.getHeadwaySeconds());
		assertEquals(GtfsExactTime.FREQUENCY_BASED,
				frequency.getNonNullExactTime());
	}

	@Test
	public void testGood() {
		/* Basic DAO testing for a known good and simple feed */
		TestBundle goodFile = loadAndValidate("good_feed");
		testGoodDao(goodFile.dao);
		TestBundle goodZip = loadAndValidate("good_feed.zip");
		testGoodDao(goodZip.dao);
	}

	@Test
	public void testMissingFile() {
		TestBundle tb = loadAndValidate("does_not_exists");
		assertEquals(1, tb.report.getReportIssues(GeneralIOError.class).size());
	}

	@Test
	public void testUnknownFormat() {
		TestBundle tb = loadAndValidate("unknown_format.zip");
		assertEquals(1, tb.report.getReportIssues(GeneralIOError.class).size());
	}

	@Test
	public void testBadEol() {
		// TODO What's wrong in this file???
		TestBundle tb = loadAndValidate("bad_eol.zip");
		// assertEquals(1,
		// tb.report.getReportItems(GeneralIOError.class).size());
	}

	@Test
	public void testUnrecognizedColumn() {
		TestBundle tb = loadAndValidate("unrecognized_columns");

		Collection<UnrecognizedColumnInfo> ucws = tb.report
				.getReportIssues(UnrecognizedColumnInfo.class);
		// TODO Enable
		// assertEquals(3, ucws.size());
		boolean agencyLange = false;
		boolean routeTextColor = false;
		boolean stopUri = false;
		for (UnrecognizedColumnInfo ucw : ucws) {
			SourceInfoWithFields siwf = ucw.getSourceInfos().get(0);
			if (siwf.getSourceInfo().getTable().getTableName()
					.equals(GtfsAgency.TABLE_NAME)
					&& siwf.getFieldNames().contains("agency_lange"))
				agencyLange = true;
			if (siwf.getSourceInfo().getTable().getTableName()
					.equals(GtfsRoute.TABLE_NAME)
					&& siwf.getFieldNames().contains("Route_Text_Color"))
				routeTextColor = true;
			if (siwf.getSourceInfo().getTable().getTableName()
					.equals(GtfsStop.TABLE_NAME)
					&& siwf.getFieldNames().contains("stop_uri"))
				stopUri = true;
		}
		assertTrue(agencyLange);
		assertTrue(routeTextColor);
		assertTrue(stopUri);

		List<MissingMandatoryColumnError> mmcs = tb.report
				.getReportIssues(MissingMandatoryColumnError.class);
		assertEquals(1, mmcs.size());
		MissingMandatoryColumnError mmc0 = mmcs.get(0);
		assertEquals(GtfsAgency.TABLE_NAME, mmc0.getSourceInfos().get(0)
				.getSourceInfo().getTable().getTableName());
		assertEquals("agency_name", mmc0.getColumnName());

		List<DuplicatedColumnError> dcs = tb.report
				.getReportIssues(DuplicatedColumnError.class);
		assertEquals(1, dcs.size());
		DuplicatedColumnError dc = dcs.get(0);
		assertEquals(GtfsAgency.TABLE_NAME, dc.getSourceInfos().get(0)
				.getSourceInfo().getTable().getTableName());
		assertTrue(dc.getSourceInfos().get(0).getFieldNames()
				.contains("agency_url"));
	}

	@Test
	public void testUnrecognizedFile() {
		TestBundle tb = loadAndValidate("unknown_file");

		Collection<UnknownFileInfo> ucws = tb.report
				.getReportIssues(UnknownFileInfo.class);
		// TODO Enable
		// assertEquals(1, ucws.size());
		boolean frecuencias = false;
		for (UnknownFileInfo ucw : ucws) {
			if (ucw.getFileName().equals("frecuencias.txt"))
				frecuencias = true;
		}
		assertTrue(frecuencias);
	}

	@Test
	public void testEmptyFile() {
		TestBundle tb = loadAndValidate("empty_file");
		Collection<EmptyTableError> etes = tb.report
				.getReportIssues(EmptyTableError.class);
		assertEquals(1, etes.size());
		EmptyTableError ete = etes.iterator().next();
		assertEquals(GtfsAgency.TABLE_NAME, ete.getTableName());
		assertTrue(tb.dao.getAgencies().isEmpty());
	}

	@Test
	public void testBadUtf8() {
		TestBundle tb = loadAndValidate("bad_utf8");
		Collection<InvalidEncodingError> iees = tb.report
				.getReportIssues(InvalidEncodingError.class);
		// TODO Enable this
		// assertEquals(5, iees.size());
		boolean agencyHeader = false;
		boolean agencyName = false;
		for (InvalidEncodingError iee : iees) {
			SourceInfoWithFields siwf = iee.getSourceInfos().get(0);
			if (siwf.getSourceInfo().getTable().getTableName()
					.equals(GtfsAgency.TABLE_NAME)) {
				if (siwf.getSourceInfo().getLineNumber() == 1
						&& siwf.getFieldNames().contains("badheader�"))
					agencyHeader = true;
				if (siwf.getSourceInfo().getLineNumber() == 2
						&& siwf.getFieldNames().contains("agency_name"))
					agencyName = true;
			}
		}
		assertTrue(agencyHeader);
		assertTrue(agencyName);
	}

	@Test
	public void testUtf16() {
		TestBundle tb = loadAndValidate("utf16");
		List<InvalidCharsetError> ices = tb.report
				.getReportIssues(InvalidCharsetError.class);
		// TODO set final count (number of tables)
		// assertEquals(1, ices.size());
		assertTrue(ices.size() > 5);
		for (InvalidCharsetError ice : ices) {
			assertEquals(StandardCharsets.UTF_16BE, ice.getCharset());
		}
	}

	@Test
	public void testMissingAgency() {
		TestBundle tb = loadAndValidate("missing_agency");
		List<MissingMandatoryTableError> mmts = tb.report
				.getReportIssues(MissingMandatoryTableError.class);
		assertEquals(1, mmts.size());
		MissingMandatoryTableError mmt = mmts.get(0);
		assertEquals(GtfsAgency.TABLE_NAME, mmt.getTableName());
		assertTrue(tb.dao.getAgencies().isEmpty());
		Collection<InvalidReferenceError> ires = tb.report
				.getReportIssues(InvalidReferenceError.class);
		assertEquals(5, ires.size());
	}

	@Test
	public void testMissingStops() {
		TestBundle tb = loadAndValidate("missing_stops");
		List<MissingMandatoryTableError> mmts = tb.report
				.getReportIssues(MissingMandatoryTableError.class);
		assertEquals(1, mmts.size());
		MissingMandatoryTableError mmt = mmts.iterator().next();
		assertEquals(GtfsStop.TABLE_NAME, mmt.getTableName());
		assertTrue(tb.dao.getStops().isEmpty());
		List<InvalidReferenceError> ires = tb.report
				.getReportIssues(InvalidReferenceError.class);
		assertEquals(28, ires.size());
		for (InvalidReferenceError ire : ires) {
			assertEquals(1, ire.getSourceInfos().size());
			SourceInfoWithFields siwf = ire.getSourceInfos().get(0);
			assertEquals(GtfsStopTime.TABLE_NAME,
					siwf.getSourceInfo().getTable().getTableName());
			assertTrue(siwf.getFieldNames().contains("stop_id"));
			assertEquals(GtfsStop.TABLE_NAME, ire.getRefTableName());
			assertEquals("stop_id", ire.getRefFieldName());
		}
		assertEquals("STAGECOACH", ires.get(0).getValue());
	}

	@Test
	public void testMissingRoutes() {
		TestBundle tb = loadAndValidate("missing_routes");
		Collection<MissingMandatoryTableError> mmts = tb.report
				.getReportIssues(MissingMandatoryTableError.class);
		assertEquals(1, mmts.size());
		MissingMandatoryTableError mmt = mmts.iterator().next();
		assertEquals(GtfsRoute.TABLE_NAME, mmt.getTableName());
		assertTrue(tb.dao.getRoutes().isEmpty());
	}

	@Test
	public void testMissingTrips() {
		TestBundle tb = loadAndValidate("missing_trips");
		Collection<MissingMandatoryTableError> mmts = tb.report
				.getReportIssues(MissingMandatoryTableError.class);
		assertEquals(1, mmts.size());
		MissingMandatoryTableError mmt = mmts.iterator().next();
		assertEquals(GtfsTrip.TABLE_NAME, mmt.getTableName());
		assertTrue(tb.dao.getTrips().isEmpty());
	}

	@Test
	public void testMissingStopTimes() {
		TestBundle tb = loadAndValidate("missing_stop_times");
		List<MissingMandatoryTableError> mmts = tb.report
				.getReportIssues(MissingMandatoryTableError.class);
		assertEquals(1, mmts.size());
		MissingMandatoryTableError mmt = mmts.get(0);
		assertEquals(GtfsStopTime.TABLE_NAME, mmt.getTableName());
		for (GtfsTrip trip : tb.dao.getTrips()) {
			assertTrue(tb.dao.getStopTimesOfTrip(trip.getId()).isEmpty());
		}
	}

	@Test
	public void testMissingDepartureTime() {
		TestBundle tb = loadAndValidate("missing_departure_time");
		List<MissingMandatoryValueError> mmvs = tb.report
				.getReportIssues(MissingMandatoryValueError.class);
		assertEquals(1, mmvs.size());
		MissingMandatoryValueError mmv = mmvs.get(0);
		assertEquals(GtfsStopTime.TABLE_NAME, mmv.getSourceInfos().get(0)
				.getSourceInfo().getTable().getTableName());
		assertEquals("departure_time",
				mmv.getSourceInfos().get(0).getFieldNames().iterator().next());
	}

	@Test
	public void testMissingCalendars() {
		TestBundle tb = loadAndValidate("missing_calendar");
		Collection<MissingMandatoryTableError> mmts = tb.report
				.getReportIssues(MissingMandatoryTableError.class);
		assertEquals(1, mmts.size());
		/*
		 * TODO If both calendars.txt and calendar_dates.txt are missing, we
		 * only report as missing calendar_dates.txt. Make a special error for
		 * mandatory table out of two?
		 */
		MissingMandatoryTableError mmt = mmts.iterator().next();
		assertEquals(GtfsCalendarDate.TABLE_NAME, mmt.getTableName());
		assertTrue(tb.dao.getCalendars().isEmpty());
		assertTrue(tb.dao.getCalendarDates().isEmpty());
	}

	@Test
	public void testInvalidRouteAgency() {
		TestBundle tb = loadAndValidate("invalid_route_agency");
		List<InvalidReferenceError> ires = tb.report
				.getReportIssues(InvalidReferenceError.class);
		assertEquals(1, ires.size());
		InvalidReferenceError ire = ires.get(0);
		assertEquals(1, ire.getSourceInfos().size());
		SourceInfoWithFields siwf = ire.getSourceInfos().get(0);
		assertEquals(GtfsRoute.TABLE_NAME,
				siwf.getSourceInfo().getTable().getTableName());
		assertTrue(siwf.getFieldNames().contains("agency_id"));
		assertEquals("DVT", ire.getValue());
		assertEquals(GtfsAgency.TABLE_NAME, ire.getRefTableName());
		assertEquals("agency_id", ire.getRefFieldName());
	}

	@Test
	public void testBadDate() {
		TestBundle tb = loadAndValidate("bad_date_format");
		List<InvalidFieldFormatError> iffes = tb.report
				.getReportIssues(InvalidFieldFormatError.class);
		assertEquals(2, iffes.size());
		InvalidFieldFormatError iffe1 = iffes.get(0);
		assertEquals(1, iffe1.getSourceInfos().size());
		SourceInfoWithFields siwf1 = iffe1.getSourceInfos().get(0);
		assertEquals(GtfsCalendar.TABLE_NAME,
				siwf1.getSourceInfo().getTable().getTableName());
		assertTrue(siwf1.getFieldNames().contains("start_date"));
		assertEquals("2007.01.01", iffe1.getValue());
		InvalidFieldFormatError iffe2 = iffes.get(1);
		assertEquals(1, iffe2.getSourceInfos().size());
		SourceInfoWithFields siwf2 = iffe2.getSourceInfos().get(0);
		assertEquals(GtfsCalendarDate.TABLE_NAME,
				siwf2.getSourceInfo().getTable().getTableName());
		assertTrue(siwf2.getFieldNames().contains("date"));
		assertEquals("2007-06-04", iffe2.getValue());
	}

	@Test
	public void testUndefinedStop() {
		TestBundle tb = loadAndValidate("undefined_stop");
		List<InvalidReferenceError> ires = tb.report
				.getReportIssues(InvalidReferenceError.class);
		assertEquals(1, ires.size());
		InvalidReferenceError ire = ires.get(0);
		assertEquals(1, ire.getSourceInfos().size());
		SourceInfoWithFields siwf = ire.getSourceInfos().get(0);
		assertEquals(GtfsStopTime.TABLE_NAME,
				siwf.getSourceInfo().getTable().getTableName());
		assertTrue(siwf.getFieldNames().contains("stop_id"));
		assertEquals("NADAR", ire.getValue());
		assertEquals(GtfsStop.TABLE_NAME, ire.getRefTableName());
		assertEquals("stop_id", ire.getRefFieldName());
	}

	@Test
	public void testDuplicateScheduleId() {
		TestBundle tb = loadAndValidate("duplicate_schedule_id");
		List<DuplicatedObjectIdError> doies = tb.report
				.getReportIssues(DuplicatedObjectIdError.class);
		assertEquals(1, doies.size());
		DuplicatedObjectIdError doie = doies.get(0);
		assertEquals(GtfsCalendar.id("WE"), doie.getDuplicatedId());
		assertEquals(2, doie.getSourceInfos().size());
		SourceInfoWithFields siwf1 = doie.getSourceInfos().get(0);
		assertEquals(GtfsCalendar.TABLE_NAME,
				siwf1.getSourceInfo().getTable().getTableName());
		assertEquals(3, siwf1.getSourceInfo().getLineNumber());
		SourceInfoWithFields siwf2 = doie.getSourceInfos().get(1);
		assertEquals(GtfsCalendar.TABLE_NAME,
				siwf2.getSourceInfo().getTable().getTableName());
		assertEquals(4, siwf2.getSourceInfo().getLineNumber());
	}

	@Test
	public void testGeospatial() {
		TestBundle tb = loadAndValidate("good_feed");

		// Test linear indexing
		LinearGeometryIndex lgi = tb.dao.getLinearGeometryIndex();
		GtfsTrip city1 = tb.dao.getTrip(GtfsTrip.id("CITY1"));
		List<GtfsStopTime> stopTimes = tb.dao.getStopTimesOfTrip(city1.getId());
		assertEquals((double) 0.0,
				lgi.getProjectedPoint(stopTimes.get(0)).getArcLengthMeters(),
				1e-10);
		GtfsStop stop0 = tb.dao.getStop(stopTimes.get(0).getStopId());
		GtfsStop stop1 = tb.dao.getStop(stopTimes.get(1).getStopId());
		GtfsStop stop2 = tb.dao.getStop(stopTimes.get(2).getStopId());
		double d01 = Geodesics.distanceMeters(stop0.getCoordinates(),
				stop1.getCoordinates());
		double d12 = Geodesics.distanceMeters(stop1.getCoordinates(),
				stop2.getCoordinates());
		double l01 = lgi.getLinearDistance(stopTimes.get(0), stopTimes.get(1));
		double l12 = lgi.getLinearDistance(stopTimes.get(1), stopTimes.get(2));
		double l02 = lgi.getLinearDistance(stopTimes.get(0), stopTimes.get(2));
		assertEquals(l02, l01 + l12, 1e-10);
		assertEquals((double) 0.0,
				lgi.getProjectedPoint(stopTimes.get(0)).getArcLengthMeters(),
				1e-10);
		assertTrue(d01 <= l01);
		assertTrue(d12 <= l12);
		assertEquals((double) 1601.2931,
				lgi.getProjectedPoint(stopTimes.get(1)).getArcLengthMeters(),
				1e-4);
		assertEquals((double) 2204.3906,
				lgi.getProjectedPoint(stopTimes.get(2)).getArcLengthMeters(),
				1e-4);

		// Test spatial indexing
		DaoSpatialIndex dsi = tb.dao.getSpatialIndex();
		Collection<GtfsStop> stops = dsi
				.getStopsAround(new GeoCoordinates(0, 0), 10000, true);
		assertTrue(stops.isEmpty());
		GtfsStop furCreek = tb.dao.getStop(GtfsStop.id("FUR_CREEK_RES"));
		GtfsStop beattyAirport = tb.dao
				.getStop(GtfsStop.id("BEATTY_AIRPORT_STATION"));
		stops = dsi.getStopsAround(furCreek.getCoordinates(), 1, true);
		assertEquals(1, stops.size());
		assertEquals(furCreek, stops.iterator().next());
		stops = dsi.getStopsAround(
				new GeoCoordinates(furCreek.getLat() + Geodesics.deltaLat(2),
						furCreek.getLon()),
				1, true);
		assertTrue(stops.isEmpty());
		double dMax = Geodesics.distanceMeters(beattyAirport.getCoordinates(),
				furCreek.getCoordinates());
		stops = dsi.getStopsAround(beattyAirport.getCoordinates(), dMax - 1,
				true);
		assertEquals(tb.dao.getStops().size() - 1, stops.size());
		stops = dsi.getStopsAround(beattyAirport.getCoordinates(), dMax + 1,
				true);
		assertEquals(tb.dao.getStops().size(), stops.size());
	}

	@Test
	public void testTooFastTravel() {
		TestBundle tb = loadAndValidate("toofast_travel");
		List<TooFastTravelIssue> tfts = tb.report
				.getReportIssues(TooFastTravelIssue.class);
		assertEquals(5, tfts.size());
		TooFastTravelIssue tft0 = tfts.get(0);
		assertEquals(GtfsStop.id("BEATTY_AIRPORT"), tft0.getStop1().getId());
		assertEquals(GtfsStop.id("BULLFROG"), tft0.getStop2().getId());
		assertEquals(ReportIssueSeverity.ERROR, tft0.getSeverity());
		assertTrue(Math.abs(66005.2 - tft0.getDistanceMeters()) < 1.0);
		assertTrue(Math.abs(100.0 - tft0.getSpeedMps()) < 1.0);

		TooFastTravelIssue tft2 = tfts.get(2);
		assertEquals(GtfsTrip.id("CITY1"), tft2.getTrip().getId());
		assertEquals(GtfsStop.id("STAGECOACH"), tft2.getStop1().getId());
		assertEquals(GtfsStop.id("EMSI"), tft2.getStop2().getId());
		assertEquals(ReportIssueSeverity.WARNING, tft2.getSeverity());
	}

	@Test
	public void testStopTooFarFromParentStation() {
		TestBundle tb = loadAndValidate("stops_toofar");
		List<StopTooFarFromParentStationIssue> stfs = tb.report
				.getReportIssues(StopTooFarFromParentStationIssue.class);
		assertEquals(2, stfs.size());
		StopTooFarFromParentStationIssue stf0 = stfs.get(0);
		assertTrue(Math.abs(3181.11 - stf0.getDistanceMeters()) < 1.0);
		assertEquals(GtfsStop.id("BEATTY_AIRPORT_3"), stf0.getStop().getId());
		assertEquals(ReportIssueSeverity.ERROR, stf0.getSeverity());

		StopTooFarFromParentStationIssue stf1 = stfs.get(1);
		assertTrue(Math.abs(762.81 - stf1.getDistanceMeters()) < 1.0);
		assertEquals(GtfsStop.id("BEATTY_AIRPORT_2"), stf1.getStop().getId());
		assertEquals(ReportIssueSeverity.WARNING, stf1.getSeverity());
	}

	@Test
	public void testEmptyCalendar() {
		TestBundle tb = loadAndValidate("empty_calendar");
		List<EmptyCalendarWarning> ecws = tb.report
				.getReportIssues(EmptyCalendarWarning.class);
		assertEquals(1, ecws.size());
		EmptyCalendarWarning ecw = ecws.get(0);
		assertEquals(GtfsCalendar.id("MONDAY"), ecw.getServiceId());
	}

	@Test
	public void testUnusedStop() {
		TestBundle tb = loadAndValidate("unused_stop");
		List<UnusedObjectWarning> uows = tb.report
				.getReportIssues(UnusedObjectWarning.class);
		assertEquals(1, uows.size());
		UnusedObjectWarning uow = uows.get(0);
		assertEquals(GtfsStop.id("BOGUS"), uow.getId());
	}

	@Test
	public void testUnusedData() {
		TestBundle tb = loadAndValidate("unused_data");
		List<UnusedObjectWarning> uows = tb.report
				.getReportIssues(UnusedObjectWarning.class);
		assertEquals(7, uows.size());
		Set<GtfsId<?, ?>> unusedIds = uows.stream()
				.map(UnusedObjectWarning::getId).collect(Collectors.toSet());
		for (GtfsId<?, ?> id : Arrays.asList(GtfsAgency.id("UNUSED_AGENCY"),
				GtfsRoute.id("UNUSED_ROUTE"),
				GtfsCalendar.id("UNUSED_CALENDAR"),
				GtfsCalendar.id("UNUSED_CALENDAR_DATE"),
				GtfsStop.id("UNUSED_STOP"), GtfsStop.id("UNUSED_STATION"),
				GtfsShape.id("UNUSED_SHAPE"))) {
			assertTrue(unusedIds.contains(id));
		}
	}

	@Test
	public void testBogusShapes() {
		TestBundle tb = loadAndValidate("bogus_shape");
		List<MissingObjectIdError> mois = tb.report
				.getReportIssues(MissingObjectIdError.class);
		assertEquals(3, mois.size());
		List<MissingMandatoryValueError> mmvs = tb.report
				.getReportIssues(MissingMandatoryValueError.class);
		assertEquals(5, mmvs.size());
		List<InvalidFieldFormatError> iffs = tb.report
				.getReportIssues(InvalidFieldFormatError.class);
		assertEquals(6, iffs.size());
		List<InvalidReferenceError> ires = tb.report
				.getReportIssues(InvalidReferenceError.class);
		assertEquals(1, ires.size());
		InvalidReferenceError ire = ires.get(0);
		assertEquals("INEXISTING_SHAPE", ire.getValue());
		List<NonIncreasingShapeDistTraveledError> nisd = tb.report
				.getReportIssues(NonIncreasingShapeDistTraveledError.class);
		assertEquals(2, nisd.size());
	}

	@Test
	public void testShapes() {
		TestBundle tb = loadAndValidate("shapes");
		IndexedReadOnlyDao dao = tb.dao;
		LinearGeometryIndex lgi = dao.getLinearGeometryIndex();

		/* A simple one segment shape (S0->S1->S2) */
		List<GtfsStopTime> stopTimes = dao
				.getStopTimesOfTrip(GtfsTrip.id("T1"));
		GtfsStopTime st0 = stopTimes.get(0);
		GtfsStopTime st1 = stopTimes.get(1);
		GtfsStopTime st2 = stopTimes.get(2);
		GtfsStop s0 = dao.getStop(st0.getStopId());
		GtfsStop s1 = dao.getStop(st1.getStopId());
		GtfsStop s2 = dao.getStop(st2.getStopId());
		double d01 = Geodesics.distanceMeters(s0.getCoordinates(),
				s1.getCoordinates());
		double d12 = Geodesics.distanceMeters(s1.getCoordinates(),
				s2.getCoordinates());
		ProjectedPoint pp0 = lgi.getProjectedPoint(st0);
		ProjectedPoint pp1 = lgi.getProjectedPoint(st1);
		ProjectedPoint pp2 = lgi.getProjectedPoint(st2);
		assertEquals(0.0, pp0.getArcLengthMeters(), 1e-4);
		assertEquals(d01, pp1.getArcLengthMeters(), 1e-4);
		assertEquals(d01 + d12, pp2.getArcLengthMeters(), 1e-4);
		assertEquals(0.0, pp0.getDistanceToShapeMeters(), 1e-4);
		assertEquals(0.0, pp0.getDistanceToShapeMeters(), 1e-4);
		assertEquals(0.0, pp0.getDistanceToShapeMeters(), 1e-4);

		/* A simple one segment shape, reversed (S2->S1->SO) */
		stopTimes = dao.getStopTimesOfTrip(GtfsTrip.id("T2"));
		st0 = stopTimes.get(0);
		st1 = stopTimes.get(1);
		st2 = stopTimes.get(2);
		s0 = dao.getStop(st0.getStopId());
		s1 = dao.getStop(st1.getStopId());
		s2 = dao.getStop(st2.getStopId());
		pp0 = lgi.getProjectedPoint(st0);
		pp1 = lgi.getProjectedPoint(st1);
		pp2 = lgi.getProjectedPoint(st2);
		assertEquals(d01 + d12, pp0.getArcLengthMeters(), 1e-4);
		assertEquals(d01 + d12, pp1.getArcLengthMeters(), 1e-4);
		assertEquals(d01 + d12, pp2.getArcLengthMeters(), 1e-4);
		assertEquals(0, pp0.getDistanceToShapeMeters(), 1e-4);
		assertEquals(d01, pp1.getDistanceToShapeMeters(), 1e-4);
		assertEquals(d01 + d12, pp2.getDistanceToShapeMeters(), 1e-4);
		assertTrue(Geodesics.distanceMeters(s0.getCoordinates(),
				pp0.getProjectedPoint()) < 1e-4);
		assertTrue(Geodesics.distanceMeters(s0.getCoordinates(),
				pp1.getProjectedPoint()) < 1e-4);
		assertTrue(Geodesics.distanceMeters(s0.getCoordinates(),
				pp2.getProjectedPoint()) < 1e-4);

		/* A backtracing 2 segment shape (S1->S3->S2) */
		stopTimes = dao.getStopTimesOfTrip(GtfsTrip.id("T3"));
		st0 = stopTimes.get(0);
		st1 = stopTimes.get(1);
		st2 = stopTimes.get(2);
		s0 = dao.getStop(st0.getStopId());
		s1 = dao.getStop(st1.getStopId());
		s2 = dao.getStop(st2.getStopId());
		pp0 = lgi.getProjectedPoint(st0);
		pp1 = lgi.getProjectedPoint(st1);
		pp2 = lgi.getProjectedPoint(st2);
		assertEquals(0.0, pp0.getArcLengthMeters(), 1e-4);
		assertEquals(d01 + d12, pp1.getArcLengthMeters(), 1e-4);
		assertEquals(d01 + d12 + d12, pp2.getArcLengthMeters(), 1e-4);
		assertEquals(0.0, pp0.getDistanceToShapeMeters(), 1e-4);
		assertEquals(0.0, pp1.getDistanceToShapeMeters(), 0.2);
		assertEquals(0.0, pp2.getDistanceToShapeMeters(), 0.4);
		assertTrue(Geodesics.distanceMeters(s0.getCoordinates(),
				pp0.getProjectedPoint()) < 1);
		assertTrue(Geodesics.distanceMeters(s1.getCoordinates(),
				pp1.getProjectedPoint()) < 1);
		assertTrue(Geodesics.distanceMeters(s2.getCoordinates(),
				pp2.getProjectedPoint()) < 1);
	}

	@Test
	public void testRouteColors() {
		TestBundle tb = loadAndValidate("route_colors");
		List<RouteColorContrastIssue> rccs = tb.report
				.getReportIssues(RouteColorContrastIssue.class);
		assertEquals(4, rccs.size());
		RouteColorContrastIssue rcc0 = rccs.get(0);
		assertEquals(0.195, rcc0.getBrightnessDelta(), 1e-3);
		RouteColorContrastIssue rcc2 = rccs.get(2);
		assertEquals(0.0, rcc2.getBrightnessDelta(), 1e-3);

		List<SimilarRouteColorWarning> srcs = tb.report
				.getReportIssues(SimilarRouteColorWarning.class);
		assertEquals(4, srcs.size());
		SimilarRouteColorWarning src0 = srcs.get(0);
		assertEquals(0.00226, src0.getColorDistance(), 1e-4);
	}

	@Test
	public void testDupTrips() {
		TestBundle tb = loadAndValidate("duplicate_trips");
		List<DuplicatedTripIssue> dtis = tb.report
				.getReportIssues(DuplicatedTripIssue.class);
		assertEquals(3, dtis.size());
		assertEquals(261, dtis.get(0).getCalendarOverlap().getDaysCount());
		// Both following dates should be Mondays
		assertEquals(GtfsLogicalDate.getDate(2007, 1, 1),
				dtis.get(0).getCalendarOverlap().getFrom());
		assertEquals(GtfsLogicalDate.getDate(2011, 12, 26),
				dtis.get(0).getCalendarOverlap().getTo());
		assertEquals(GtfsTrip.id("STBA1"), dtis.get(0).getTrip1().getId());
		assertEquals(GtfsTrip.id("STBA2"), dtis.get(0).getTrip2().getId());
		assertEquals(1305, dtis.get(1).getCalendarOverlap().getDaysCount());
		assertEquals(GtfsTrip.id("STBA1"), dtis.get(1).getTrip1().getId());
		assertEquals(GtfsTrip.id("STBA5"), dtis.get(1).getTrip2().getId());
		assertEquals(261, dtis.get(2).getCalendarOverlap().getDaysCount());
		assertEquals(GtfsTrip.id("STBA2"), dtis.get(2).getTrip1().getId());
		assertEquals(GtfsTrip.id("STBA5"), dtis.get(2).getTrip2().getId());
	}

	@Test
	public void testRouteNames() {
		TestBundle tb = loadAndValidate("route_names");
		List<MissingMandatoryValueError> mmvs = tb.report
				.getReportIssues(MissingMandatoryValueError.class);
		assertEquals(1, mmvs.size());
		MissingMandatoryValueError mmv0 = mmvs.get(0);
		assertEquals(GtfsRoute.TABLE_NAME, mmv0.getSourceInfos().get(0)
				.getSourceInfo().getTable().getTableName());
		List<InvalidFieldFormatError> iffs = tb.report
				.getReportIssues(InvalidFieldFormatError.class);
		assertEquals(3, iffs.size());
	}

	@Test
	public void testOverlappingBlocks() {
		TestBundle tb = loadAndValidate("overlapping_blockid");
		List<OverlappingBlockIdIssue> obis = tb.report
				.getReportIssues(OverlappingBlockIdIssue.class);
		assertEquals(3, obis.size());
		OverlappingBlockIdIssue obi0 = obis.get(0);
		assertEquals(GtfsBlockId.fromValue("B1"), obi0.getBlockId());
	}

	@Test
	public void testTimeTravels() {
		TestBundle tb = loadAndValidate("timetravels");
		List<TimeTravelAtStopError> ttas = tb.report
				.getReportIssues(TimeTravelAtStopError.class);
		assertEquals(2, ttas.size());
		TimeTravelAtStopError tta0 = ttas.get(0);
		assertEquals(GtfsStop.id("STAGECOACH"), tta0.getStopTime().getStopId());
		assertEquals(GtfsTripStopSequence.fromSequence(0),
				tta0.getStopTime().getStopSequence());
		List<TimeTravelError> tts = tb.report
				.getReportIssues(TimeTravelError.class);
		assertEquals(1, tts.size());
		TimeTravelError tt0 = tts.get(0);
		assertEquals(GtfsStop.id("DADAN"), tt0.getStop1().getId());
		assertEquals(GtfsStop.id("NADAV"), tt0.getStop2().getId());
	}

	@Test
	public void testBogusCalendars() {
		TestBundle tb = loadAndValidate("bogus_calendars");
		List<InvalidFieldValueError> ifvs = tb.report
				.getReportIssues(InvalidFieldValueError.class);
		assertEquals(1, ifvs.size());
		InvalidFieldValueError ifv0 = ifvs.get(0);
		assertTrue(ifv0.getSourceInfos().get(0).getFieldNames()
				.contains("monday"));
		assertTrue(ifv0.getSourceInfos().get(0).getFieldNames()
				.contains("sunday"));
	}

	@Test
	public void testInvalidStopTimeType() {
		TestBundle tb = loadAndValidate("invalid_stoptime_type");
		List<WrongStopTimeStopTypeError> wsts = tb.report
				.getReportIssues(WrongStopTimeStopTypeError.class);
		assertEquals(1, wsts.size());
		WrongStopTimeStopTypeError wst0 = wsts.get(0);
		assertEquals(GtfsStop.id("BEATTY_AIRPORT_STATION"),
				wst0.getStop().getId());
		assertEquals(GtfsStopType.STATION, wst0.getStop().getType());
	}

	@Test
	public void testBogusFrequencies() {
		TestBundle tb = loadAndValidate("bogus_frequencies");
		List<InvalidFieldValueError> ifvs = tb.report
				.getReportIssues(InvalidFieldValueError.class);
		assertEquals(1, ifvs.size());
		List<InvalidFieldFormatError> iffs = tb.report
				.getReportIssues(InvalidFieldFormatError.class);
		assertEquals(1, iffs.size());
		List<MissingMandatoryValueError> mmvs = tb.report
				.getReportIssues(MissingMandatoryValueError.class);
		assertEquals(5, mmvs.size());
	}

	@Test
	public void testMBTA42951766() {
		TestBundle tb = loadAndValidate("MBTA_42951766");
		IndexedReadOnlyDao dao = tb.dao;
		LinearGeometryIndex lgi = dao.getLinearGeometryIndex();
		List<GtfsStopTime> stopTimes = dao
				.getStopTimesOfTrip(GtfsTrip.id("42951766"));
		// Average distance from stop to shape
		double avgDist = 0.0;
		// Average factor between stop distance and shape linear distance
		double avgK = 0.0;
		GtfsStopTime prevStopTime = null;
		GtfsStop prevStop = null;
		ProjectedPoint prevPp = null;
		for (GtfsStopTime stopTime : stopTimes) {
			GtfsStop stop = dao.getStop(stopTime.getStopId());
			ProjectedPoint pp = lgi.getProjectedPoint(stopTime);
			avgDist += pp.getDistanceToShapeMeters();
			assertTrue(pp.getDistanceToShapeMeters() < 10.0);
			double d = Geodesics.fastDistanceMeters(stop.getCoordinates(),
					pp.getProjectedPoint());
			assertEquals(pp.getDistanceToShapeMeters(), d, 1e-4);
			if (prevStopTime != null) {
				double ld = pp.getArcLengthMeters()
						- prevPp.getArcLengthMeters();
				double sd = Geodesics.fastDistanceMeters(
						prevStop.getCoordinates(), stop.getCoordinates());
				// Add 10m to take into account cases where stops are very close
				double k = (ld + 10) / (sd + 10);
				avgK += k;
				assertTrue(k < 2.2);
			}
			prevStopTime = stopTime;
			prevStop = stop;
			prevPp = pp;
		}
		avgDist /= stopTimes.size();
		avgK /= stopTimes.size();
		assertTrue(avgDist < 6.0);
		assertTrue(avgK < 1.1);
	}

	@Test
	public void testAachener74431429() {
		TestBundle tb = loadAndValidate("aachener_74431429");
		assertEquals(0,
				tb.report.issuesCountOfSeverity(ReportIssueSeverity.ERROR));
		assertEquals(0,
				tb.report.issuesCountOfSeverity(ReportIssueSeverity.CRITICAL));
		List<TooFastTravelIssue> tftis = tb.report
				.getReportIssues(TooFastTravelIssue.class);
		assertEquals(1, tftis.size());
		TooFastTravelIssue tfti0 = tftis.get(0);
		assertEquals(14.66, tfti0.getSpeedMps(), 1e-2);
		assertEquals(439.82, tfti0.getDistanceMeters(), 1e-2);
	}

	@Test
	public void testAachener73069683() {
		TestBundle tb = loadAndValidate("aachener_73069683");
		assertEquals(0,
				tb.report.issuesCountOfSeverity(ReportIssueSeverity.WARNING));
		// One error: stop too far from projected shape.
		assertEquals(1,
				tb.report.issuesCountOfSeverity(ReportIssueSeverity.ERROR));
		assertEquals(0,
				tb.report.issuesCountOfSeverity(ReportIssueSeverity.CRITICAL));

		List<StopTooFarFromShapeIssue> stfs = tb.report
				.getReportIssues(StopTooFarFromShapeIssue.class);
		assertEquals(1, stfs.size());
		StopTooFarFromShapeIssue stf0 = stfs.get(0);
		assertEquals(GtfsStop.id("000000003934"), stf0.getStop().getId());
		assertEquals(GtfsTripStopSequence.fromSequence(26),
				stf0.getStopSequence());
		double d = Geodesics.fastDistanceMeters(
				new GeoCoordinates(51.055061, 6.226201),
				stf0.getProjectedPoint());
		assertTrue(d < 1.0);
		assertEquals(209.12, stf0.getDistanceMeters(), 1e-2);
	}

	@Test
	public void testLoadingAll() {
		File base = new File("src/test/resources/data");
		for (String file : base.list()) {
			TestBundle tb = loadAndValidate(file);
			// Just check if it does not throw an exception
		}
	}

	@Test
	public void testLoadingAll2() {
		// Just check if it does not throw an exception
//		File base = new File("src/test/resources/xdata");
//		for (String file : base.list()) {
//			System.out.println("===================================");
//			System.out.println("Loading and testing: " + file);
//			TestBundle tb = loadAndValidate(file, "src/test/resources/xdata/");
//			System.out.println("-----------------------------------");
//		}
	}
}
