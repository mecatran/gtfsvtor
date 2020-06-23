package com.mecatran.gtfsvtor.test;

import static com.mecatran.gtfsvtor.test.TestUtils.loadAndValidate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
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
import com.mecatran.gtfsvtor.lib.GtfsVtorOptions.ShapePointsDaoMode;
import com.mecatran.gtfsvtor.lib.GtfsVtorOptions.StopTimesDaoMode;
import com.mecatran.gtfsvtor.loader.DataObjectSourceInfo;
import com.mecatran.gtfsvtor.model.GtfsAgency;
import com.mecatran.gtfsvtor.model.GtfsBlockId;
import com.mecatran.gtfsvtor.model.GtfsCalendar;
import com.mecatran.gtfsvtor.model.GtfsCalendarDate;
import com.mecatran.gtfsvtor.model.GtfsCalendarDateExceptionType;
import com.mecatran.gtfsvtor.model.GtfsDropoffType;
import com.mecatran.gtfsvtor.model.GtfsExactTime;
import com.mecatran.gtfsvtor.model.GtfsFareAttribute;
import com.mecatran.gtfsvtor.model.GtfsFareRule;
import com.mecatran.gtfsvtor.model.GtfsFeedInfo;
import com.mecatran.gtfsvtor.model.GtfsFrequency;
import com.mecatran.gtfsvtor.model.GtfsId;
import com.mecatran.gtfsvtor.model.GtfsLevel;
import com.mecatran.gtfsvtor.model.GtfsLogicalDate;
import com.mecatran.gtfsvtor.model.GtfsLogicalTime;
import com.mecatran.gtfsvtor.model.GtfsPathway;
import com.mecatran.gtfsvtor.model.GtfsPathwayMode;
import com.mecatran.gtfsvtor.model.GtfsPaymentMethod;
import com.mecatran.gtfsvtor.model.GtfsPickupType;
import com.mecatran.gtfsvtor.model.GtfsRoute;
import com.mecatran.gtfsvtor.model.GtfsShape;
import com.mecatran.gtfsvtor.model.GtfsShapePoint;
import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.model.GtfsStopTime;
import com.mecatran.gtfsvtor.model.GtfsStopType;
import com.mecatran.gtfsvtor.model.GtfsTransfer;
import com.mecatran.gtfsvtor.model.GtfsTransferType;
import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.model.GtfsTripAndTimes;
import com.mecatran.gtfsvtor.model.GtfsTripDirectionId;
import com.mecatran.gtfsvtor.model.GtfsTripStopSequence;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.SourceRefWithFields;
import com.mecatran.gtfsvtor.reporting.issues.DifferentHeadsignsIssue;
import com.mecatran.gtfsvtor.reporting.issues.DifferentStationTooCloseWarning;
import com.mecatran.gtfsvtor.reporting.issues.DuplicatedColumnError;
import com.mecatran.gtfsvtor.reporting.issues.DuplicatedFareRuleWarning;
import com.mecatran.gtfsvtor.reporting.issues.DuplicatedObjectIdError;
import com.mecatran.gtfsvtor.reporting.issues.DuplicatedStopSequenceError;
import com.mecatran.gtfsvtor.reporting.issues.DuplicatedTripIssue;
import com.mecatran.gtfsvtor.reporting.issues.EmptyCalendarWarning;
import com.mecatran.gtfsvtor.reporting.issues.EmptyTableError;
import com.mecatran.gtfsvtor.reporting.issues.FirstOrLastStopTimeMissingError;
import com.mecatran.gtfsvtor.reporting.issues.GeneralIOError;
import com.mecatran.gtfsvtor.reporting.issues.InconsistentNumberOfFieldsWarning;
import com.mecatran.gtfsvtor.reporting.issues.InvalidCharsetError;
import com.mecatran.gtfsvtor.reporting.issues.InvalidCoordinateError;
import com.mecatran.gtfsvtor.reporting.issues.InvalidEncodingError;
import com.mecatran.gtfsvtor.reporting.issues.InvalidFieldFormatError;
import com.mecatran.gtfsvtor.reporting.issues.InvalidFieldValueIssue;
import com.mecatran.gtfsvtor.reporting.issues.InvalidReferenceError;
import com.mecatran.gtfsvtor.reporting.issues.MissingMandatoryColumnError;
import com.mecatran.gtfsvtor.reporting.issues.MissingMandatoryTableError;
import com.mecatran.gtfsvtor.reporting.issues.MissingMandatoryValueError;
import com.mecatran.gtfsvtor.reporting.issues.MissingObjectIdError;
import com.mecatran.gtfsvtor.reporting.issues.NoServiceError;
import com.mecatran.gtfsvtor.reporting.issues.NoServiceExceptionWarning;
import com.mecatran.gtfsvtor.reporting.issues.NonIncreasingShapeDistTraveledError;
import com.mecatran.gtfsvtor.reporting.issues.OverlappingBlockIdIssue;
import com.mecatran.gtfsvtor.reporting.issues.RouteColorContrastIssue;
import com.mecatran.gtfsvtor.reporting.issues.SimilarRouteColorWarning;
import com.mecatran.gtfsvtor.reporting.issues.StopTooCloseIssue;
import com.mecatran.gtfsvtor.reporting.issues.StopTooCloseToOriginError;
import com.mecatran.gtfsvtor.reporting.issues.StopTooFarFromParentStationIssue;
import com.mecatran.gtfsvtor.reporting.issues.StopTooFarFromShapeIssue;
import com.mecatran.gtfsvtor.reporting.issues.TimeTravelAtStopError;
import com.mecatran.gtfsvtor.reporting.issues.TimeTravelError;
import com.mecatran.gtfsvtor.reporting.issues.TooFastTravelIssue;
import com.mecatran.gtfsvtor.reporting.issues.TooFastWalkingSpeed;
import com.mecatran.gtfsvtor.reporting.issues.TooManyDaysWithoutServiceIssue;
import com.mecatran.gtfsvtor.reporting.issues.UnknownFileInfo;
import com.mecatran.gtfsvtor.reporting.issues.UnrecognizedColumnInfo;
import com.mecatran.gtfsvtor.reporting.issues.UnusedObjectWarning;
import com.mecatran.gtfsvtor.reporting.issues.UselessCalendarDateWarning;
import com.mecatran.gtfsvtor.reporting.issues.UselessValueWarning;
import com.mecatran.gtfsvtor.reporting.issues.WrongDropOffPickUpTypeForSplitTripsIssue;
import com.mecatran.gtfsvtor.reporting.issues.WrongPathwayStopTypeError;
import com.mecatran.gtfsvtor.reporting.issues.WrongStopTimeStopTypeError;
import com.mecatran.gtfsvtor.reporting.issues.WrongTransferStopTypeError;
import com.mecatran.gtfsvtor.test.TestUtils.TestBundle;
import com.mecatran.gtfsvtor.test.TestUtils.TestScenario;

public class TestGtfs {

	private void testGoodDao(IndexedReadOnlyDao dao) {

		GtfsFeedInfo feedInfo = dao.getFeedInfo();
		assertNotNull(feedInfo);
		assertEquals("Autorité de passage de démonstration",
				feedInfo.getFeedPublisherName());
		assertEquals("http://google.com", feedInfo.getFeedPublisherUrl());
		assertEquals(new Locale("en"), feedInfo.getFeedLang());
		assertEquals(GtfsLogicalDate.getDate(2007, 1, 1),
				feedInfo.getFeedStartDate());
		assertEquals(GtfsLogicalDate.getDate(2011, 12, 31),
				feedInfo.getFeedEndDate());

		assertEquals(1, dao.getAgencies().count());
		GtfsAgency dta = dao.getAgency(GtfsAgency.id("DTA"));
		assertNotNull(dta);
		assertEquals("Autorité de passage de démonstration", dta.getName());

		assertEquals(5, dao.getRoutes().count());
		GtfsRoute city = dao.getRoute(GtfsRoute.id("CITY"));
		assertNotNull(city);
		assertEquals("Ō", city.getShortName());
		assertEquals("Bar Circle", city.getLongName());

		assertEquals(12, dao.getStops().count());
		GtfsStop nadav = dao.getStop(GtfsStop.id("NADAV"));
		assertNotNull(nadav);
		assertEquals(GtfsStopType.STOP, nadav.getType());
		assertEquals("North Ave / D Ave N (Demo)", nadav.getName());

		GtfsStop inexistant = dao.getStop(GtfsStop.id("FOOBAR"));
		assertNull(inexistant);

		GtfsStop beattyEntranceNorth = dao
				.getStop(GtfsStop.id("BEATTY_AIRPORT_ENTRANCE_NORTH"));
		assertNotNull(beattyEntranceNorth);
		assertEquals(GtfsStopType.ENTRANCE, beattyEntranceNorth.getType());
		assertEquals(GtfsLevel.id("level_1"), beattyEntranceNorth.getLevelId());

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
				.getCalendarDates(GtfsCalendar.id("FULLW"))
				.collect(Collectors.toList());
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

		List<GtfsCalendar.Id> calIds;
		calIds = calIndex
				.getCalendarIdsOnDate(GtfsLogicalDate.getDate(2007, 1, 1))
				.collect(Collectors.toList());
		assertEquals(1, calIds.size());
		assertEquals(GtfsCalendar.id("FULLW"), calIds.iterator().next());
		calIds = calIndex
				.getCalendarIdsOnDate(GtfsLogicalDate.getDate(2007, 6, 4))
				.collect(Collectors.toList());
		assertEquals(0, calIds.size());
		calIds = calIndex
				.getCalendarIdsOnDate(GtfsLogicalDate.getDate(2011, 12, 31))
				.collect(Collectors.toList());
		assertEquals(2, calIds.size());
		assertTrue(calIds.contains(GtfsCalendar.id("WE")));
		assertTrue(calIds.contains(GtfsCalendar.id("FULLW")));
		calIds = calIndex
				.getCalendarIdsOnDate(GtfsLogicalDate.getDate(2012, 1, 1))
				.collect(Collectors.toList());
		assertEquals(0, calIds.size());

		Collection<GtfsTrip> trips = dao.getTrips()
				.collect(Collectors.toList());
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
				.getTripAndTimes(GtfsTrip.id("CITY1")).getStopTimes();
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
				.getTripAndTimes(GtfsTrip.id("FOOBAR")).getStopTimes();
		assertTrue(unknownStopTimes.isEmpty());

		List<GtfsFrequency> frequencies = dao.getFrequencies()
				.collect(Collectors.toList());
		assertEquals(11, frequencies.size());
		frequencies = dao.getFrequenciesOfTrip(GtfsTrip.id("STBA"))
				.collect(Collectors.toList());
		assertEquals(1, frequencies.size());
		GtfsFrequency frequency = frequencies.get(0);
		assertEquals(GtfsTrip.id("STBA"), frequency.getTripId());
		assertEquals(GtfsLogicalTime.getTime(6, 0, 0),
				frequency.getStartTime());
		assertEquals(GtfsLogicalTime.getTime(22, 0, 0), frequency.getEndTime());
		assertEquals(Integer.valueOf(1800), frequency.getHeadwaySeconds());
		assertEquals(GtfsExactTime.FREQUENCY_BASED,
				frequency.getNonNullExactTime());

		Collection<GtfsTransfer> transfers = dao.getTransfers()
				.collect(Collectors.toList());
		assertEquals(2, transfers.size());
		GtfsTransfer t1 = dao.getTransfer(GtfsStop.id("NADAV"),
				GtfsStop.id("NANAA"), null, null, null, null);
		assertEquals(GtfsTransferType.NONE, t1.getNonNullType());
		GtfsTransfer t2 = dao.getTransfer(GtfsStop.id("EMSI"),
				GtfsStop.id("NANAA"), null, null, null, null);
		assertEquals(GtfsTransferType.TIMED, t2.getNonNullType());
		assertEquals(Integer.valueOf(1200), t2.getMinTransferTime());

		assertEquals(2, dao.getPathways().count());
		GtfsPathway p1 = dao.getPathway(GtfsPathway.id("p1"));
		assertEquals(GtfsStop.id("BEATTY_AIRPORT_ENTRANCE_SOUTH"),
				p1.getFromStopId());
		assertEquals(GtfsStop.id("BEATTY_AIRPORT"), p1.getToStopId());
		assertEquals(GtfsPathwayMode.WALKWAY, p1.getPathwayMode());

		assertEquals(2, dao.getFareAttributes().count());
		GtfsFareAttribute p = dao.getFareAttribute(GtfsFareAttribute.id("p"));
		assertEquals(Currency.getInstance("USD"), p.getCurrencyType());
		assertEquals(GtfsPaymentMethod.ON_BOARD, p.getPaymentMethod());
		assertEquals(1.25f, p.getPrice(), 0.0f);
		assertNull(p.getAgencyId());
		Collection<GtfsFareRule> prules = dao.getRulesOfFare(p.getId())
				.collect(Collectors.toList());
		assertEquals(3, prules.size());
		for (GtfsFareRule prule : prules) {
			assertNotNull(prule.getRouteId());
		}
	}

	@Test
	public void testGood() {
		/* Basic DAO testing for a known good and simple feed */
		for (String file : Arrays.asList("good_feed", "good_feed.zip")) {
			TestScenario testScenario = new TestScenario(file);
			TestBundle goodFile = testScenario.run();
			testGoodDao(goodFile.dao);
		}
	}

	@Test
	public void testOnlyCalendarDates() {
		/* Good feed, only calendar_date is present */
		TestBundle tb = loadAndValidate("only_calendar_dates");
		SortedSet<GtfsLogicalDate> fullw = tb.dao.getCalendarIndex()
				.getCalendarApplicableDates(GtfsCalendar.id("FULLW"));
		assertEquals(1, fullw.size());
		assertEquals(GtfsLogicalDate.getDate(2007, 6, 4),
				fullw.iterator().next());
		SortedSet<GtfsLogicalDate> we = tb.dao.getCalendarIndex()
				.getCalendarApplicableDates(GtfsCalendar.id("WE"));
		assertEquals(1, we.size());
		assertEquals(GtfsLogicalDate.getDate(2007, 6, 5), we.iterator().next());
	}

	@Test
	public void testMissingFile() {
		TestBundle tb = loadAndValidate("does_not_exists");
		assertEquals(1, tb.issuesCountOfCategory(GeneralIOError.class));
	}

	@Test
	public void testUnknownFormat() {
		TestBundle tb = loadAndValidate("unknown_format.zip");
		assertEquals(1, tb.issuesCountOfCategory(GeneralIOError.class));
	}

	@Test
	public void testBadEol() {
		// See TODO file for a list of issues to validate
		TestBundle tb = loadAndValidate("bad_eol.zip");
		// assertEquals(1,
		// tb.report.getReportItems(GeneralIOError.class).size());
	}

	@Test
	public void testContainsNull() {
		TestBundle tb = loadAndValidate("contains_null");
		List<InvalidEncodingError> iees = tb
				.issuesOfCategory(InvalidEncodingError.class);
		assertEquals(1, iees.size());
		InvalidEncodingError iee0 = iees.get(0);
		assertEquals("E NULL to the right\0NULL to the left (Demo)",
				iee0.getValue());
	}

	@Test
	public void testExtraRowCells() {
		TestBundle tb = loadAndValidate("extra_row_cells");
		List<InconsistentNumberOfFieldsWarning> inofs = tb
				.issuesOfCategory(InconsistentNumberOfFieldsWarning.class);
		assertEquals(2, inofs.size());
		InconsistentNumberOfFieldsWarning inof0 = inofs.get(0);
		assertEquals(7, inof0.getNumberOfFields());
		assertEquals(6, inof0.getNumberOfHeaderColumns());
		assertEquals(GtfsRoute.TABLE_NAME,
				inof0.getSourceRefs().get(0).getSourceRef().getTableName());
		InconsistentNumberOfFieldsWarning inof1 = inofs.get(1);
		assertEquals(4, inof1.getNumberOfFields());
	}

	@Test
	public void testMissingRowCells() {
		TestBundle tb = loadAndValidate("missing_row_cells");
		List<InconsistentNumberOfFieldsWarning> inofs = tb
				.issuesOfCategory(InconsistentNumberOfFieldsWarning.class);
		assertEquals(1, inofs.size());
		InconsistentNumberOfFieldsWarning inof0 = inofs.get(0);
		assertEquals(6, inof0.getNumberOfFields());
		assertEquals(7, inof0.getNumberOfHeaderColumns());
		assertEquals(GtfsRoute.TABLE_NAME,
				inof0.getSourceRefs().get(0).getSourceRef().getTableName());
	}

	@Test
	public void testUnrecognizedColumn() {
		TestBundle tb = loadAndValidate("unrecognized_columns");
		Collection<UnrecognizedColumnInfo> ucws = tb
				.issuesOfCategory(UnrecognizedColumnInfo.class);
		// TODO Enable
		// assertEquals(3, ucws.size());
		boolean agencyLange = false;
		boolean routeTextColor = false;
		boolean stopUri = false;
		for (UnrecognizedColumnInfo ucw : ucws) {
			SourceRefWithFields siwf = ucw.getSourceRefs().get(0);
			if (siwf.getSourceRef().getTableName().equals(GtfsAgency.TABLE_NAME)
					&& siwf.getFieldNames().contains("agency_lange"))
				agencyLange = true;
			if (siwf.getSourceRef().getTableName().equals(GtfsRoute.TABLE_NAME)
					&& siwf.getFieldNames().contains("Route_Text_Color"))
				routeTextColor = true;
			if (siwf.getSourceRef().getTableName().equals(GtfsStop.TABLE_NAME)
					&& siwf.getFieldNames().contains("stop_uri"))
				stopUri = true;
		}
		assertTrue(agencyLange);
		assertTrue(routeTextColor);
		assertTrue(stopUri);

		List<MissingMandatoryColumnError> mmcs = tb
				.issuesOfCategory(MissingMandatoryColumnError.class);
		assertEquals(1, mmcs.size());
		MissingMandatoryColumnError mmc0 = mmcs.get(0);
		assertEquals(GtfsAgency.TABLE_NAME,
				mmc0.getSourceRefs().get(0).getSourceRef().getTableName());
		assertEquals("agency_name", mmc0.getColumnName());

		List<DuplicatedColumnError> dcs = tb
				.issuesOfCategory(DuplicatedColumnError.class);
		assertEquals(1, dcs.size());
		DuplicatedColumnError dc = dcs.get(0);
		assertEquals(GtfsAgency.TABLE_NAME,
				dc.getSourceRefs().get(0).getSourceRef().getTableName());
		assertTrue(dc.getSourceRefs().get(0).getFieldNames()
				.contains("agency_url"));
	}

	@Test
	public void testUnrecognizedFile() {
		TestBundle tb = loadAndValidate("unknown_file");
		Collection<UnknownFileInfo> ucws = tb
				.issuesOfCategory(UnknownFileInfo.class);
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
		Collection<EmptyTableError> etes = tb
				.issuesOfCategory(EmptyTableError.class);
		assertEquals(1, etes.size());
		EmptyTableError ete = etes.iterator().next();
		assertEquals(GtfsAgency.TABLE_NAME, ete.getTableName());
		assertTrue(tb.dao.getAgencies().count() == 0);
	}

	@Test
	public void testBadUtf8() {
		TestBundle tb = loadAndValidate("bad_utf8");
		Collection<InvalidEncodingError> iees = tb
				.issuesOfCategory(InvalidEncodingError.class);
		// TODO Enable this
		// assertEquals(5, iees.size());
		boolean agencyHeader = false;
		boolean agencyName = false;
		for (InvalidEncodingError iee : iees) {
			SourceRefWithFields siwf = iee.getSourceRefs().get(0);
			if (siwf.getSourceRef().getTableName()
					.equals(GtfsAgency.TABLE_NAME)) {
				if (siwf.getSourceRef().getLineNumber() == 1
						&& siwf.getFieldNames().contains("badheader�"))
					agencyHeader = true;
				if (siwf.getSourceRef().getLineNumber() == 2
						&& siwf.getFieldNames().contains("agency_name"))
					agencyName = true;
			}
		}
		assertTrue(agencyHeader);
		assertTrue(agencyName);
	}

	@Test
	public void testUtf8Bol() {
		// agency.txt contains UTF8 BOM
		TestBundle tb = loadAndValidate("utf8bom");
		assertEquals(1, tb.dao.getAgencies().count());
		assertEquals("Demo Transit Authority",
				tb.dao.getAgency(GtfsAgency.id("DTA")).getName());
	}

	@Test
	public void testUtf16() {
		TestBundle tb = loadAndValidate("utf16");
		List<InvalidCharsetError> ices = tb
				.issuesOfCategory(InvalidCharsetError.class);
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
		List<MissingMandatoryTableError> mmts = tb
				.issuesOfCategory(MissingMandatoryTableError.class);
		assertEquals(1, mmts.size());
		MissingMandatoryTableError mmt = mmts.get(0);
		assertEquals(GtfsAgency.TABLE_NAME, mmt.getTableName());
		assertTrue(tb.dao.getAgencies().count() == 0);
		Collection<InvalidReferenceError> ires = tb
				.issuesOfCategory(InvalidReferenceError.class);
		assertEquals(5, ires.size());
	}

	@Test
	public void testMissingStops() {
		TestBundle tb = loadAndValidate("missing_stops");
		List<MissingMandatoryTableError> mmts = tb
				.issuesOfCategory(MissingMandatoryTableError.class);
		assertEquals(1, mmts.size());
		MissingMandatoryTableError mmt = mmts.iterator().next();
		assertEquals(GtfsStop.TABLE_NAME, mmt.getTableName());
		assertTrue(tb.dao.getStops().count() == 0);
		List<InvalidReferenceError> ires = tb
				.issuesOfCategory(InvalidReferenceError.class);
		assertEquals(28, ires.size());
		for (InvalidReferenceError ire : ires) {
			assertEquals(1, ire.getSourceRefs().size());
			SourceRefWithFields siwf = ire.getSourceRefs().get(0);
			assertEquals(GtfsStopTime.TABLE_NAME,
					siwf.getSourceRef().getTableName());
			assertTrue(siwf.getFieldNames().contains("stop_id"));
			assertEquals(GtfsStop.TABLE_NAME, ire.getRefTableName());
			assertEquals("stop_id", ire.getRefFieldName());
		}
		assertEquals("STAGECOACH", ires.get(0).getValue());
	}

	@Test
	public void testMissingRoutes() {
		TestBundle tb = loadAndValidate("missing_routes");
		Collection<MissingMandatoryTableError> mmts = tb
				.issuesOfCategory(MissingMandatoryTableError.class);
		assertEquals(1, mmts.size());
		MissingMandatoryTableError mmt = mmts.iterator().next();
		assertEquals(GtfsRoute.TABLE_NAME, mmt.getTableName());
		assertTrue(tb.dao.getRoutes().count() == 0);
	}

	@Test
	public void testMissingTrips() {
		TestBundle tb = loadAndValidate("missing_trips");
		List<MissingMandatoryTableError> mmts = tb
				.issuesOfCategory(MissingMandatoryTableError.class);
		assertEquals(1, mmts.size());
		MissingMandatoryTableError mmt = mmts.get(0);
		assertEquals(GtfsTrip.TABLE_NAME, mmt.getTableName());
		assertTrue(tb.dao.getTrips().count() == 0);
	}

	@Test
	public void testMissingWeekdayColumn() {
		TestBundle tb = loadAndValidate("missing_weekday_column");
		List<MissingMandatoryColumnError> mmcs = tb
				.issuesOfCategory(MissingMandatoryColumnError.class);
		assertEquals(1, mmcs.size());
		MissingMandatoryColumnError mmc = mmcs.get(0);
		assertEquals(GtfsCalendar.TABLE_NAME,
				mmc.getSourceRefs().get(0).getSourceRef().getTableName());
		assertEquals("thursday", mmc.getColumnName());
	}

	@Test
	public void testMissingStopTimes() {
		TestBundle tb = loadAndValidate("missing_stop_times");
		List<MissingMandatoryTableError> mmts = tb
				.issuesOfCategory(MissingMandatoryTableError.class);
		assertEquals(1, mmts.size());
		MissingMandatoryTableError mmt = mmts.get(0);
		assertEquals(GtfsStopTime.TABLE_NAME, mmt.getTableName());
		tb.dao.getTrips().forEach(trip -> assertTrue(
				tb.dao.getTripAndTimes(trip.getId()).getStopTimes().isEmpty()));
	}

	@Test
	public void testMissingDepartureTime() {
		TestBundle tb = loadAndValidate("missing_departure_time");
		List<MissingMandatoryValueError> mmvs = tb
				.issuesOfCategory(MissingMandatoryValueError.class);
		assertEquals(1, mmvs.size());
		MissingMandatoryValueError mmv = mmvs.get(0);
		assertEquals(GtfsStopTime.TABLE_NAME,
				mmv.getSourceRefs().get(0).getSourceRef().getTableName());
		assertEquals("departure_time",
				mmv.getSourceRefs().get(0).getFieldNames().iterator().next());
	}

	@Test
	public void testMissingEndpointTime() {
		TestBundle tb = loadAndValidate("missing_endpoint_times");
		List<FirstOrLastStopTimeMissingError> flms = tb
				.issuesOfCategory(FirstOrLastStopTimeMissingError.class);
		assertEquals(2, flms.size());
		FirstOrLastStopTimeMissingError flm0 = flms.get(0);
		assertEquals(GtfsTrip.id("AB2"), flm0.getStopTime().getTripId());
		assertEquals(GtfsStop.id("BULLFROG"), flm0.getStopTime().getStopId());
		assertEquals(GtfsTripStopSequence.fromSequence(1),
				flm0.getStopTime().getStopSequence());
		FirstOrLastStopTimeMissingError flm1 = flms.get(1);
		assertEquals(GtfsTrip.id("BFC2"), flm1.getStopTime().getTripId());
		assertEquals(GtfsStop.id("BULLFROG"), flm1.getStopTime().getStopId());
		assertEquals(GtfsTripStopSequence.fromSequence(2),
				flm1.getStopTime().getStopSequence());
	}

	@Test
	public void testMissingCalendars() {
		TestBundle tb = loadAndValidate("missing_calendar");
		List<MissingMandatoryTableError> mmts = tb
				.issuesOfCategory(MissingMandatoryTableError.class);
		assertEquals(1, mmts.size());
		/*
		 * TODO If both calendars.txt and calendar_dates.txt are missing, we
		 * only report as missing calendar_dates.txt. Make a special error for
		 * mandatory table out of two?
		 */
		MissingMandatoryTableError mmt = mmts.get(0);
		assertEquals(GtfsCalendarDate.TABLE_NAME, mmt.getTableName());
		assertTrue(tb.dao.getCalendars().count() == 0);
		assertTrue(tb.dao.getCalendarDates().count() == 0);
	}

	@Test
	public void testMissingColumn() {
		TestBundle tb = loadAndValidate("missing_column");
		List<MissingMandatoryColumnError> mmcs = tb
				.issuesOfCategory(MissingMandatoryColumnError.class);
		assertEquals(1, mmcs.size());
		MissingMandatoryColumnError mmc0 = mmcs.get(0);
		assertEquals("agency_name", mmc0.getColumnName());
	}

	@Test
	public void testInvalidRouteAgency() {
		TestBundle tb = loadAndValidate("invalid_route_agency");
		List<InvalidReferenceError> ires = tb
				.issuesOfCategory(InvalidReferenceError.class);
		assertEquals(1, ires.size());
		InvalidReferenceError ire = ires.get(0);
		assertEquals(1, ire.getSourceRefs().size());
		SourceRefWithFields siwf = ire.getSourceRefs().get(0);
		assertEquals(GtfsRoute.TABLE_NAME, siwf.getSourceRef().getTableName());
		assertTrue(siwf.getFieldNames().contains("agency_id"));
		assertEquals("DVT", ire.getValue());
		assertEquals(GtfsAgency.TABLE_NAME, ire.getRefTableName());
		assertEquals("agency_id", ire.getRefFieldName());
	}

	@Test
	public void testBadDate() {
		TestBundle tb = loadAndValidate("bad_date_format");
		List<InvalidFieldFormatError> iffes = tb
				.issuesOfCategory(InvalidFieldFormatError.class);
		assertEquals(2, iffes.size());
		InvalidFieldFormatError iffe1 = iffes.get(0);
		assertEquals(1, iffe1.getSourceRefs().size());
		SourceRefWithFields siwf1 = iffe1.getSourceRefs().get(0);
		assertEquals(GtfsCalendar.TABLE_NAME,
				siwf1.getSourceRef().getTableName());
		assertTrue(siwf1.getFieldNames().contains("start_date"));
		assertEquals("2007.01.01", iffe1.getValue());
		InvalidFieldFormatError iffe2 = iffes.get(1);
		assertEquals(1, iffe2.getSourceRefs().size());
		SourceRefWithFields siwf2 = iffe2.getSourceRefs().get(0);
		assertEquals(GtfsCalendarDate.TABLE_NAME,
				siwf2.getSourceRef().getTableName());
		assertTrue(siwf2.getFieldNames().contains("date"));
		assertEquals("2007-06-04", iffe2.getValue());
	}

	@Test
	public void testBadCoords() {
		TestBundle tb = loadAndValidate("bad_coords");
		List<InvalidCoordinateError> icrds = tb
				.issuesOfCategory(InvalidCoordinateError.class);
		assertEquals(4, icrds.size());
	}

	@Test
	public void testUndefinedStop() {
		TestBundle tb = loadAndValidate("undefined_stop");
		List<InvalidReferenceError> ires = tb
				.issuesOfCategory(InvalidReferenceError.class);
		assertEquals(1, ires.size());
		InvalidReferenceError ire = ires.get(0);
		assertEquals(1, ire.getSourceRefs().size());
		SourceRefWithFields siwf = ire.getSourceRefs().get(0);
		assertEquals(GtfsStopTime.TABLE_NAME,
				siwf.getSourceRef().getTableName());
		assertTrue(siwf.getFieldNames().contains("stop_id"));
		assertEquals("NADAR", ire.getValue());
		assertEquals(GtfsStop.TABLE_NAME, ire.getRefTableName());
		assertEquals("stop_id", ire.getRefFieldName());
	}

	@Test
	public void testDuplicateScheduleId() {
		TestBundle tb = loadAndValidate("duplicate_schedule_id");
		List<DuplicatedObjectIdError> doies = tb
				.issuesOfCategory(DuplicatedObjectIdError.class);
		assertEquals(1, doies.size());
		DuplicatedObjectIdError doie = doies.get(0);
		assertEquals(GtfsCalendar.id("WE"), doie.getDuplicatedId());
		assertEquals(2, doie.getSourceRefs().size());
		SourceRefWithFields siwf1 = doie.getSourceRefs().get(0);
		assertEquals(GtfsCalendar.TABLE_NAME,
				siwf1.getSourceRef().getTableName());
		assertEquals(3, siwf1.getSourceRef().getLineNumber());
		DataObjectSourceInfo si1 = tb.report
				.getSourceInfo(siwf1.getSourceRef());
		assertEquals(3, si1.getLineNumber());
		assertEquals(GtfsCalendar.TABLE_NAME, si1.getTable().getTableName());
		assertEquals(10, si1.getFields().size());
		assertEquals("WE", si1.getFields().get(0));
		assertEquals("20101231", si1.getFields().get(9));
		SourceRefWithFields siwf2 = doie.getSourceRefs().get(1);
		assertEquals(GtfsCalendar.TABLE_NAME,
				siwf2.getSourceRef().getTableName());
		assertEquals(4, siwf2.getSourceRef().getLineNumber());
		DataObjectSourceInfo si2 = tb.report
				.getSourceInfo(siwf2.getSourceRef());
		assertEquals(4, si2.getLineNumber());
		assertEquals(GtfsCalendar.TABLE_NAME, si2.getTable().getTableName());
		assertEquals(10, si2.getFields().size());
		assertEquals("WE", si2.getFields().get(0));
		assertEquals("20101231", si2.getFields().get(9));
	}

	@Test
	public void testDuplicateIds() {
		TestBundle tb = loadAndValidate("duplicate_ids");
		List<DuplicatedObjectIdError> doies = tb
				.issuesOfCategory(DuplicatedObjectIdError.class);
		assertEquals(6, doies.size());
		List<DuplicatedFareRuleWarning> dfrws = tb
				.issuesOfCategory(DuplicatedFareRuleWarning.class);
		assertEquals(1, dfrws.size());
	}

	@Test
	public void testGeospatial() {
		TestBundle tb = loadAndValidate("good_feed");

		// Test linear indexing
		LinearGeometryIndex lgi = tb.dao.getLinearGeometryIndex();
		GtfsTrip city1 = tb.dao.getTrip(GtfsTrip.id("CITY1"));
		List<GtfsStopTime> stopTimes = tb.dao.getTripAndTimes(city1.getId())
				.getStopTimes();
		assertEquals((double) 0.0,
				lgi.getProjectedPoint(stopTimes.get(0)).getArcLengthMeters(),
				1e-10);
		GtfsStop stop0 = tb.dao.getStop(stopTimes.get(0).getStopId());
		GtfsStop stop1 = tb.dao.getStop(stopTimes.get(1).getStopId());
		GtfsStop stop2 = tb.dao.getStop(stopTimes.get(2).getStopId());
		double d01 = Geodesics.distanceMeters(stop0.getValidCoordinates().get(),
				stop1.getValidCoordinates().get());
		double d12 = Geodesics.distanceMeters(stop1.getValidCoordinates().get(),
				stop2.getValidCoordinates().get());
		double l01 = lgi.getLinearDistance(stopTimes.get(0), stopTimes.get(1));
		double l12 = lgi.getLinearDistance(stopTimes.get(1), stopTimes.get(2));
		double l02 = lgi.getLinearDistance(stopTimes.get(0), stopTimes.get(2));
		assertEquals(l02, l01 + l12, 1e-10);
		assertEquals((double) 0.0,
				lgi.getProjectedPoint(stopTimes.get(0)).getArcLengthMeters(),
				1e-10);
		assertTrue(d01 <= l01);
		assertTrue(d12 <= l12);
		assertEquals((double) 1601.29,
				lgi.getProjectedPoint(stopTimes.get(1)).getArcLengthMeters(),
				1e-2);
		assertEquals((double) 2204.38,
				lgi.getProjectedPoint(stopTimes.get(2)).getArcLengthMeters(),
				1e-2);

		// Test spatial indexing
		DaoSpatialIndex dsi = tb.dao.getSpatialIndex();
		Collection<GtfsStop> stops = dsi
				.getStopsAround(new GeoCoordinates(0, 0), 10000, true)
				.collect(Collectors.toList());
		assertTrue(stops.isEmpty());
		GtfsStop furCreek = tb.dao.getStop(GtfsStop.id("FUR_CREEK_RES"));
		GtfsStop beattyAirport = tb.dao
				.getStop(GtfsStop.id("BEATTY_AIRPORT_STATION"));
		stops = dsi
				.getStopsAround(furCreek.getValidCoordinates().get(), 1, true)
				.collect(Collectors.toList());
		assertEquals(1, stops.size());
		assertEquals(furCreek, stops.iterator().next());
		stops = dsi
				.getStopsAround(new GeoCoordinates(
						furCreek.getLat() + Geodesics.deltaLat(2),
						furCreek.getLon()), 1, true)
				.collect(Collectors.toList());
		assertTrue(stops.isEmpty());
		double dMax = Geodesics.distanceMeters(
				beattyAirport.getValidCoordinates().get(),
				furCreek.getValidCoordinates().get());
		stops = dsi.getStopsAround(beattyAirport.getValidCoordinates().get(),
				dMax - 1, true).collect(Collectors.toList());
		assertEquals(tb.dao.getStops().count() - 1, stops.size());
		stops = dsi.getStopsAround(beattyAirport.getValidCoordinates().get(),
				dMax + 1, true).collect(Collectors.toList());
		assertEquals(tb.dao.getStops().count(), stops.size());
	}

	@Test
	public void testTooFastTravel() {
		TestBundle tb = loadAndValidate("toofast_travel");
		List<TooFastTravelIssue> tfts = tb
				.issuesOfCategory(TooFastTravelIssue.class);
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
		List<StopTooFarFromParentStationIssue> stfs = tb
				.issuesOfCategory(StopTooFarFromParentStationIssue.class);
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
	public void testStopTooClose() {
		TestBundle tb = loadAndValidate("duplicate_stop");
		List<StopTooCloseIssue> stcs = tb
				.issuesOfCategory(StopTooCloseIssue.class);
		assertEquals(1, stcs.size());
		StopTooCloseIssue stc0 = stcs.get(0);
		// Is the order 1 / 2 stable?
		assertEquals(GtfsStop.id("BULLFROG"), stc0.getStop1().getId());
		assertEquals(GtfsStop.id("FROG"), stc0.getStop2().getId());
	}

	@Test
	public void testDuplicateStopSequence() {
		TestBundle tb = loadAndValidate("duplicate_stop_sequence");
		List<DuplicatedStopSequenceError> dsss = tb
				.issuesOfCategory(DuplicatedStopSequenceError.class);
		assertEquals(1, dsss.size());
		DuplicatedStopSequenceError dss0 = dsss.get(0);
		assertEquals(GtfsTripStopSequence.fromSequence(10),
				dss0.getStopSequence());
		assertEquals(GtfsTrip.id("CITY1"), dss0.getTrip().getId());
	}

	@Test
	public void testEmptyCalendar() {
		TestBundle tb = loadAndValidate("empty_calendar");
		List<EmptyCalendarWarning> ecws = tb
				.issuesOfCategory(EmptyCalendarWarning.class);
		assertEquals(1, ecws.size());
		EmptyCalendarWarning ecw = ecws.get(0);
		assertEquals(GtfsCalendar.id("MONDAY"), ecw.getServiceId());
	}

	@Test
	public void testUnusedStop() {
		TestBundle tb = loadAndValidate("unused_stop");
		List<UnusedObjectWarning> uows = tb
				.issuesOfCategory(UnusedObjectWarning.class);
		assertEquals(1, uows.size());
		UnusedObjectWarning uow = uows.get(0);
		assertEquals(GtfsStop.id("BOGUS"), uow.getId());
	}

	@Test
	public void testUnusedData() {
		TestBundle tb = loadAndValidate("unused_data");
		List<UnusedObjectWarning> uows = tb
				.issuesOfCategory(UnusedObjectWarning.class);
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
		List<MissingObjectIdError> mois = tb
				.issuesOfCategory(MissingObjectIdError.class);
		assertEquals(3, mois.size());
		List<MissingMandatoryValueError> mmvs = tb
				.issuesOfCategory(MissingMandatoryValueError.class);
		assertEquals(5, mmvs.size());
		List<InvalidFieldFormatError> iffs = tb
				.issuesOfCategory(InvalidFieldFormatError.class);
		assertEquals(6, iffs.size());
		List<InvalidReferenceError> ires = tb
				.issuesOfCategory(InvalidReferenceError.class);
		assertEquals(1, ires.size());
		InvalidReferenceError ire = ires.get(0);
		assertEquals("INEXISTING_SHAPE", ire.getValue());
		List<NonIncreasingShapeDistTraveledError> nisd = tb
				.issuesOfCategory(NonIncreasingShapeDistTraveledError.class);
		assertEquals(2, nisd.size());
	}

	@Test
	public void testShapes() {
		TestBundle tb = loadAndValidate("shapes");
		IndexedReadOnlyDao dao = tb.dao;
		LinearGeometryIndex lgi = dao.getLinearGeometryIndex();

		/* A simple one segment shape (S0->S1->S2) */
		List<GtfsStopTime> stopTimes = dao.getTripAndTimes(GtfsTrip.id("T1"))
				.getStopTimes();
		GtfsStopTime st0 = stopTimes.get(0);
		GtfsStopTime st1 = stopTimes.get(1);
		GtfsStopTime st2 = stopTimes.get(2);
		GtfsStop s0 = dao.getStop(st0.getStopId());
		GtfsStop s1 = dao.getStop(st1.getStopId());
		GtfsStop s2 = dao.getStop(st2.getStopId());
		double d01 = Geodesics.distanceMeters(s0.getValidCoordinates().get(),
				s1.getValidCoordinates().get());
		double d12 = Geodesics.distanceMeters(s1.getValidCoordinates().get(),
				s2.getValidCoordinates().get());
		ProjectedPoint pp0 = lgi.getProjectedPoint(st0);
		ProjectedPoint pp1 = lgi.getProjectedPoint(st1);
		ProjectedPoint pp2 = lgi.getProjectedPoint(st2);
		assertEquals(0.0, pp0.getArcLengthMeters(), 1e-2);
		assertEquals(d01, pp1.getArcLengthMeters(), 1e-2);
		assertEquals(d01 + d12, pp2.getArcLengthMeters(), 1e-2);
		assertEquals(0.0, pp0.getDistanceToShapeMeters(), 1e-2);
		assertEquals(0.0, pp0.getDistanceToShapeMeters(), 1e-2);
		assertEquals(0.0, pp0.getDistanceToShapeMeters(), 1e-2);

		/* A simple one segment shape, reversed (S2->S1->SO) */
		stopTimes = dao.getTripAndTimes(GtfsTrip.id("T2")).getStopTimes();
		st0 = stopTimes.get(0);
		st1 = stopTimes.get(1);
		st2 = stopTimes.get(2);
		s0 = dao.getStop(st0.getStopId());
		s1 = dao.getStop(st1.getStopId());
		s2 = dao.getStop(st2.getStopId());
		pp0 = lgi.getProjectedPoint(st0);
		pp1 = lgi.getProjectedPoint(st1);
		pp2 = lgi.getProjectedPoint(st2);
		assertEquals(d01 + d12, pp0.getArcLengthMeters(), 1e-2);
		assertEquals(d01 + d12, pp1.getArcLengthMeters(), 1e-2);
		assertEquals(d01 + d12, pp2.getArcLengthMeters(), 1e-2);
		assertEquals(0, pp0.getDistanceToShapeMeters(), 1e-2);
		assertEquals(d01, pp1.getDistanceToShapeMeters(), 1e-2);
		assertEquals(d01 + d12, pp2.getDistanceToShapeMeters(), 1e-2);
		assertTrue(Geodesics.distanceMeters(s0.getValidCoordinates().get(),
				pp0.getProjectedPoint()) < 1e-2);
		assertTrue(Geodesics.distanceMeters(s0.getValidCoordinates().get(),
				pp1.getProjectedPoint()) < 1e-2);
		assertTrue(Geodesics.distanceMeters(s0.getValidCoordinates().get(),
				pp2.getProjectedPoint()) < 1e-2);

		/* A backtracing 2 segment shape (S1->S3->S2) */
		stopTimes = dao.getTripAndTimes(GtfsTrip.id("T3")).getStopTimes();
		st0 = stopTimes.get(0);
		st1 = stopTimes.get(1);
		st2 = stopTimes.get(2);
		s0 = dao.getStop(st0.getStopId());
		s1 = dao.getStop(st1.getStopId());
		s2 = dao.getStop(st2.getStopId());
		pp0 = lgi.getProjectedPoint(st0);
		pp1 = lgi.getProjectedPoint(st1);
		pp2 = lgi.getProjectedPoint(st2);
		assertEquals(0.0, pp0.getArcLengthMeters(), 1e-2);
		assertEquals(d01 + d12, pp1.getArcLengthMeters(), 1e-2);
		assertEquals(d01 + d12 + d12, pp2.getArcLengthMeters(), 1e-2);
		assertEquals(0.0, pp0.getDistanceToShapeMeters(), 1e-2);
		assertEquals(0.0, pp1.getDistanceToShapeMeters(), 0.2);
		assertEquals(0.0, pp2.getDistanceToShapeMeters(), 0.4);
		assertTrue(Geodesics.distanceMeters(s0.getValidCoordinates().get(),
				pp0.getProjectedPoint()) < 1);
		assertTrue(Geodesics.distanceMeters(s1.getValidCoordinates().get(),
				pp1.getProjectedPoint()) < 1);
		assertTrue(Geodesics.distanceMeters(s2.getValidCoordinates().get(),
				pp2.getProjectedPoint()) < 1);
	}

	@Test
	public void testRouteColors() {
		TestBundle tb = loadAndValidate("route_colors");
		List<RouteColorContrastIssue> rccs = tb
				.issuesOfCategory(RouteColorContrastIssue.class);
		assertEquals(4, rccs.size());
		RouteColorContrastIssue rcc0 = rccs.get(0);
		assertEquals(19.5, rcc0.getBrightnessDeltaPercent(), 0.1);
		RouteColorContrastIssue rcc2 = rccs.get(2);
		assertEquals(0.0, rcc2.getBrightnessDeltaPercent(), 0.1);

		List<SimilarRouteColorWarning> srcs = tb
				.issuesOfCategory(SimilarRouteColorWarning.class);
		assertEquals(4, srcs.size());
		SimilarRouteColorWarning src0 = srcs.get(0);
		assertEquals(0.00226, src0.getColorDistance(), 1e-4);
	}

	@Test
	public void testDupTrips() {
		TestBundle tb = loadAndValidate("duplicate_trips");
		List<DuplicatedTripIssue> dtis = tb
				.issuesOfCategory(DuplicatedTripIssue.class);
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
		List<MissingMandatoryValueError> mmvs = tb
				.issuesOfCategory(MissingMandatoryValueError.class);
		assertEquals(1, mmvs.size());
		MissingMandatoryValueError mmv0 = mmvs.get(0);
		assertEquals(GtfsRoute.TABLE_NAME,
				mmv0.getSourceRefs().get(0).getSourceRef().getTableName());
		List<InvalidFieldFormatError> iffs = tb
				.issuesOfCategory(InvalidFieldFormatError.class);
		assertEquals(1, iffs.size());
		List<InvalidFieldValueIssue> ifvs = tb
				.issuesOfCategory(InvalidFieldValueIssue.class);
		assertEquals(1, ifvs.size());
	}

	@Test
	public void testOverlappingBlocks() {
		TestBundle tb = loadAndValidate("overlapping_blockid");
		List<OverlappingBlockIdIssue> obis = tb
				.issuesOfCategory(OverlappingBlockIdIssue.class);
		assertEquals(3, obis.size());
		OverlappingBlockIdIssue obi0 = obis.get(0);
		assertEquals(GtfsBlockId.fromValue("B1"), obi0.getBlockId());
	}

	@Test
	public void testSplitJoinedTrips() {
		TestBundle tb = loadAndValidate("split_or_joined_trips");
		List<WrongDropOffPickUpTypeForSplitTripsIssue> obis = tb
				.issuesOfCategory(
						WrongDropOffPickUpTypeForSplitTripsIssue.class);
		assertEquals(2, obis.size());
		List<DifferentHeadsignsIssue> dhs = tb
				.issuesOfCategory(DifferentHeadsignsIssue.class);
		assertEquals(1, dhs.size());
	}

	@Test
	public void testTimeTravels() {
		TestBundle tb = loadAndValidate("timetravels");
		List<TimeTravelAtStopError> ttas = tb
				.issuesOfCategory(TimeTravelAtStopError.class);
		assertEquals(2, ttas.size());
		TimeTravelAtStopError tta0 = ttas.get(0);
		assertEquals(GtfsStop.id("STAGECOACH"), tta0.getStopTime().getStopId());
		assertEquals(GtfsTripStopSequence.fromSequence(0),
				tta0.getStopTime().getStopSequence());
		List<TimeTravelError> tts = tb.issuesOfCategory(TimeTravelError.class);
		assertEquals(1, tts.size());
		TimeTravelError tt0 = tts.get(0);
		assertEquals(GtfsStop.id("DADAN"), tt0.getStop1().getId());
		assertEquals(GtfsStop.id("NADAV"), tt0.getStop2().getId());
	}

	@Test
	public void testBogusCalendars() {
		TestBundle tb = loadAndValidate("bogus_calendars");
		List<InvalidFieldValueIssue> ifvs = tb
				.issuesOfCategory(InvalidFieldValueIssue.class);
		assertEquals(1, ifvs.size());
		InvalidFieldValueIssue ifv0 = ifvs.get(0);
		assertTrue(
				ifv0.getSourceRefs().get(0).getFieldNames().contains("monday"));
		assertTrue(
				ifv0.getSourceRefs().get(0).getFieldNames().contains("sunday"));
	}

	@Test
	public void testInvalidStopTimeType() {
		TestBundle tb = loadAndValidate("invalid_stoptime_type");
		List<WrongStopTimeStopTypeError> wsts = tb
				.issuesOfCategory(WrongStopTimeStopTypeError.class);
		assertEquals(1, wsts.size());
		WrongStopTimeStopTypeError wst0 = wsts.get(0);
		assertEquals(GtfsStop.id("BEATTY_AIRPORT_STATION"),
				wst0.getStop().getId());
		assertEquals(GtfsStopType.STATION, wst0.getStop().getType());
	}

	@Test
	public void testBogusFrequencies() {
		TestBundle tb = loadAndValidate("bogus_frequencies");
		List<InvalidFieldValueIssue> ifvs = tb
				.issuesOfCategory(InvalidFieldValueIssue.class);
		assertEquals(1, ifvs.size());
		List<InvalidFieldFormatError> iffs = tb
				.issuesOfCategory(InvalidFieldFormatError.class);
		assertEquals(1, iffs.size());
		List<MissingMandatoryValueError> mmvs = tb
				.issuesOfCategory(MissingMandatoryValueError.class);
		assertEquals(3, mmvs.size());
	}

	@Test
	public void testTooManyDaysWoService() {
		TestBundle tb = loadAndValidate("toomanydayswoservice");
		List<TooManyDaysWithoutServiceIssue> tmws = tb
				.issuesOfCategory(TooManyDaysWithoutServiceIssue.class);
		assertEquals(1, tmws.size());
		TooManyDaysWithoutServiceIssue tmw0 = tmws.get(0);
		assertEquals(8, tmw0.getNumberOfDays());
		assertEquals(GtfsLogicalDate.getDate(2007, 6, 4), tmw0.getFromDate());
		assertEquals(GtfsLogicalDate.getDate(2007, 6, 11), tmw0.getToDate());
	}

	@Test
	public void testNoService() {
		TestBundle tb = loadAndValidate("noservice");
		List<NoServiceError> nses = tb.issuesOfCategory(NoServiceError.class);
		assertEquals(1, nses.size());
	}

	@Test
	public void testBogusTransfers() {
		TestBundle tb = loadAndValidate("bogus_transfers");
		assertEquals(2, tb.issuesCountOfCategory(TooFastWalkingSpeed.class));
		assertEquals(3,
				tb.issuesOfCategory(InvalidFieldValueIssue.class).size());
		assertEquals(2, tb.issuesCountOfCategory(UselessValueWarning.class));
		assertEquals(1, tb.issuesCountOfCategory(InvalidReferenceError.class));
		assertEquals(1,
				tb.issuesCountOfCategory(WrongTransferStopTypeError.class));
		assertEquals(1,
				tb.issuesCountOfCategory(DuplicatedObjectIdError.class));
	}

	@Test
	public void testBogusTransfersExtended() {
		TestBundle tb = loadAndValidate("bogus_transfers_extended");
		assertEquals(1, tb.issuesCountOfCategory(InvalidReferenceError.class));
		assertEquals(1,
				tb.issuesCountOfCategory(DuplicatedObjectIdError.class));
		assertEquals(0, tb.issuesCountOfCategory(UnrecognizedColumnInfo.class));
	}

	@Test
	public void testBogusPathways() {
		TestBundle tb = loadAndValidate("bogus_pathways");
		assertEquals(1,
				tb.issuesCountOfCategory(DuplicatedObjectIdError.class));
		assertEquals(1,
				tb.issuesCountOfCategory(MissingMandatoryColumnError.class));
		assertEquals(1,
				tb.issuesCountOfCategory(WrongPathwayStopTypeError.class));
		assertEquals(2,
				tb.issuesCountOfCategory(InvalidFieldFormatError.class));
		assertEquals(2, tb.issuesCountOfCategory(InvalidReferenceError.class));
		assertEquals(8,
				tb.issuesCountOfCategory(MissingMandatoryValueError.class));
	}

	@Test
	public void testBogusFares() {
		TestBundle tb = loadAndValidate("bogus_fares");
		assertEquals(5,
				tb.issuesCountOfCategory(InvalidFieldFormatError.class));
		assertEquals(1,
				tb.issuesCountOfCategory(DuplicatedObjectIdError.class));
		assertEquals(1, tb.issuesCountOfCategory(InvalidReferenceError.class));
	}

	@Test
	public void testRepeatedRouteName() {
		TestBundle tb = loadAndValidate("repeated_route_name");
		List<InvalidFieldValueIssue> ifvs = tb
				.issuesOfCategory(InvalidFieldValueIssue.class);
		assertEquals(1, ifvs.size());
		InvalidFieldValueIssue ifv0 = ifvs.get(0);
		assertEquals(3, ifv0.getSourceRefs().size());
	}

	@Test
	public void testNegativeStopSequence() {
		TestBundle tb = loadAndValidate("negative_stop_sequence");
		List<InvalidFieldFormatError> iffs = tb
				.issuesOfCategory(InvalidFieldFormatError.class);
		assertEquals(1, iffs.size());
		InvalidFieldFormatError iff0 = iffs.get(0);
		assertEquals("-2", iff0.getValue());
	}

	@Test
	public void testSpaceInHeader() {
		TestBundle tb = loadAndValidate("space_header");
		assertEquals(0,
				tb.issuesCountOfCategory(MissingMandatoryValueError.class));
		assertEquals(0, tb.issuesCountOfCategory(MissingObjectIdError.class));
	}

	@Test
	public void testNoServiceException() {
		TestBundle tb = loadAndValidate("noservice_exception");
		assertEquals(1,
				tb.issuesCountOfCategory(NoServiceExceptionWarning.class));
		assertEquals(3,
				tb.issuesCountOfCategory(UselessCalendarDateWarning.class));
	}

	@Test
	public void testDifferentStationTooClose() {
		TestBundle tb = loadAndValidate("different_station_too_close");
		assertEquals(1, tb
				.issuesCountOfCategory(DifferentStationTooCloseWarning.class));
	}

	@Test
	public void testInterleavedStopTimes() {
		for (StopTimesDaoMode daoMode : Arrays.asList(StopTimesDaoMode.AUTO,
				StopTimesDaoMode.PACKED, StopTimesDaoMode.UNSORTED)) {
			for (int max = 3; max < 20; max += 3) {
				// Having a small max interleaving factor will force overflow
				// Check that this will work anyway
				TestScenario scenario = new TestScenario(
						"interleaved_stoptimes");
				scenario.maxStopTimesInterleaving = max;
				scenario.stopTimesDaoMode = daoMode;
				TestBundle tb = scenario.run();
				assertEquals(0,
						tb.issuesCountOfSeverities(ReportIssueSeverity.ERROR,
								ReportIssueSeverity.CRITICAL));
				assertEquals(20, tb.dao.getStopTimesCount());
				for (int i = 1; i <= 10; i++) {
					GtfsTrip.Id tripId = GtfsTrip.id("AB" + i);
					List<GtfsStopTime> stopTimes = tb.dao
							.getTripAndTimes(tripId).getStopTimes();
					assertEquals(2, stopTimes.size());
					assertEquals(1,
							stopTimes.get(0).getStopSequence().getSequence());
					assertEquals(2,
							stopTimes.get(1).getStopSequence().getSequence());
					assertEquals(tripId, stopTimes.get(0).getTripId());
					assertEquals(
							i <= 5 ? GtfsStop.id("BEATTY_AIRPORT")
									: GtfsStop.id("BULLFROG"),
							stopTimes.get(0).getStopId());
				}
			}
		}
	}

	@Test
	public void testMBTA42951766() {
		TestBundle tb = loadAndValidate("MBTA_42951766");
		IndexedReadOnlyDao dao = tb.dao;
		LinearGeometryIndex lgi = dao.getLinearGeometryIndex();
		List<GtfsStopTime> stopTimes = dao
				.getTripAndTimes(GtfsTrip.id("42951766")).getStopTimes();
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
			double d = Geodesics.fastDistanceMeters(
					stop.getValidCoordinates().get(), pp.getProjectedPoint());
			assertEquals(pp.getDistanceToShapeMeters(), d, 1e-4);
			if (prevStopTime != null) {
				double ld = pp.getArcLengthMeters()
						- prevPp.getArcLengthMeters();
				double sd = Geodesics.fastDistanceMeters(
						prevStop.getValidCoordinates().get(),
						stop.getValidCoordinates().get());
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
		List<TooFastTravelIssue> tftis = tb
				.issuesOfCategory(TooFastTravelIssue.class);
		assertEquals(0, tftis.size());
		GtfsTripAndTimes trip = tb.dao.getTripAndTimes(GtfsTrip.id("74431429"));
		List<GtfsStopTime> stopTimes = trip.getStopTimes();
		assertEquals(439.81, tb.dao.getLinearGeometryIndex()
				.getLinearDistance(stopTimes.get(12), stopTimes.get(13)), 1e-2);
	}

	@Test
	public void testAachener73069683() {
		TestBundle tb = loadAndValidate("aachener_73069683");
		assertEquals(1,
				tb.report.issuesCountOfSeverity(ReportIssueSeverity.WARNING));
		// One error: stop too far from projected shape.
		assertEquals(1,
				tb.report.issuesCountOfSeverity(ReportIssueSeverity.ERROR));
		assertEquals(0,
				tb.report.issuesCountOfSeverity(ReportIssueSeverity.CRITICAL));

		List<StopTooFarFromShapeIssue> stfs = tb
				.issuesOfCategory(StopTooFarFromShapeIssue.class);
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
	public void testSwegZeroCoordinates() {
		TestBundle tb = loadAndValidate("sweg_zero_coordinates");
		List<StopTooCloseToOriginError> stctoes = tb
				.issuesOfCategory(StopTooCloseToOriginError.class);
		assertEquals(1, stctoes.size());
	}

	@Test
	public void testSingleAgency() {
		TestBundle tb = loadAndValidate("single_agency");
		List<MissingMandatoryValueError> mmvs = tb
				.issuesOfCategory(MissingMandatoryValueError.class);
		// agency_id is optional, if feed contains only a single agencys
		assertEquals(0, mmvs.size());
		List<MissingMandatoryColumnError> mmces = tb
				.issuesOfCategory(MissingMandatoryColumnError.class);
		// agency_id column is optional, if feed contains only a single agencys
		assertEquals(0, mmces.size());
		List<UnusedObjectWarning> uows = tb
				.issuesOfCategory(UnusedObjectWarning.class);
		// agency may not be refered, if feed contains only a single agency
		assertEquals(0, uows.size());
	}

	@Test
	public void testGoodFeedWithVariousStopTimeDaoMode() {
		for (StopTimesDaoMode daoMode : Arrays.asList(StopTimesDaoMode.AUTO,
				StopTimesDaoMode.PACKED, StopTimesDaoMode.UNSORTED)) {
			for (String file : Arrays.asList("good_feed", "good_feed.zip")) {
				TestScenario testScenario = new TestScenario(file);
				testScenario.maxStopTimesInterleaving = 5;
				testScenario.stopTimesDaoMode = daoMode;
				TestBundle goodFile = testScenario.run();
				testGoodDao(goodFile.dao);
			}
		}
	}

	@Test
	public void testGoodFeedWithVariousShapePointDaoMode() {
		for (ShapePointsDaoMode daoMode : Arrays.asList(ShapePointsDaoMode.AUTO,
				ShapePointsDaoMode.PACKED, ShapePointsDaoMode.UNSORTED)) {
			for (String file : Arrays.asList("good_feed", "good_feed.zip")) {
				TestScenario testScenario = new TestScenario(file);
				testScenario.maxShapePointsInterleaving = 3;
				testScenario.shapePointsDaoMode = daoMode;
				TestBundle goodFile = testScenario.run();
				testGoodDao(goodFile.dao);
			}
		}
	}

	@Test
	public void testRandomShapesWithVariousShapePointDaoMode() {
		for (ShapePointsDaoMode daoMode : Arrays.asList(ShapePointsDaoMode.AUTO,
				ShapePointsDaoMode.UNSORTED, ShapePointsDaoMode.PACKED)) {
			TestScenario testScenario = new TestScenario("MBTA_random_shapes");
			testScenario.maxShapePointsInterleaving = 3;
			testScenario.shapePointsDaoMode = daoMode;
			TestBundle tb = testScenario.run();
			tb.dao.getShapeIds().forEach(shapeId -> {
				List<GtfsShapePoint> shapePoints = tb.dao
						.getPointsOfShape(shapeId);
				int lastSeq = 0;
				for (GtfsShapePoint shapePoint : shapePoints) {
					assertTrue(lastSeq < shapePoint.getPointSequence()
							.getSequence());
					lastSeq = shapePoint.getPointSequence().getSequence();
					assertEquals(shapeId, shapePoint.getShapeId());
				}
			});
			assertEquals(1649, tb.dao.getShapeIds().flatMap(
					shapeId -> tb.dao.getPointsOfShape(shapeId).stream())
					.count());
		}
	}

	@Test
	public void testLoadingAll() {
		// Disabled: we already test all the test sets
		// File base = new File("src/test/resources/data");
		// for (String file : base.list()) {
		// TestBundle tb = loadAndValidate(file);
		// Just check if it does not throw an exception
		// }
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
