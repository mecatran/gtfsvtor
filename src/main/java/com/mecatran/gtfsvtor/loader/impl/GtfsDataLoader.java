package com.mecatran.gtfsvtor.loader.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import com.mecatran.gtfsvtor.dao.ReadOnlyDao;
import com.mecatran.gtfsvtor.loader.DataLoader;
import com.mecatran.gtfsvtor.loader.DataObjectSourceInfo;
import com.mecatran.gtfsvtor.loader.DataRow;
import com.mecatran.gtfsvtor.loader.DataRowConverter;
import com.mecatran.gtfsvtor.loader.DataTable;
import com.mecatran.gtfsvtor.loader.NamedTabularDataSource;
import com.mecatran.gtfsvtor.model.DataObjectSourceRef;
import com.mecatran.gtfsvtor.model.GtfsAgency;
import com.mecatran.gtfsvtor.model.GtfsCalendar;
import com.mecatran.gtfsvtor.model.GtfsCalendarDate;
import com.mecatran.gtfsvtor.model.GtfsFareAttribute;
import com.mecatran.gtfsvtor.model.GtfsFareRule;
import com.mecatran.gtfsvtor.model.GtfsFeedInfo;
import com.mecatran.gtfsvtor.model.GtfsFrequency;
import com.mecatran.gtfsvtor.model.GtfsLevel;
import com.mecatran.gtfsvtor.model.GtfsPathway;
import com.mecatran.gtfsvtor.model.GtfsRoute;
import com.mecatran.gtfsvtor.model.GtfsRouteType;
import com.mecatran.gtfsvtor.model.GtfsShape;
import com.mecatran.gtfsvtor.model.GtfsShapePoint;
import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.model.GtfsStopTime;
import com.mecatran.gtfsvtor.model.GtfsTransfer;
import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.model.GtfsZone;
import com.mecatran.gtfsvtor.model.impl.SimpleGtfsShapePoint;
import com.mecatran.gtfsvtor.model.impl.SimpleGtfsStopTime;
import com.mecatran.gtfsvtor.model.impl.SmallGtfsShapePoint;
import com.mecatran.gtfsvtor.model.impl.SmallGtfsStopTime;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.DuplicatedColumnError;
import com.mecatran.gtfsvtor.reporting.issues.EmptyTableError;
import com.mecatran.gtfsvtor.reporting.issues.InconsistentNumberOfFieldsWarning;
import com.mecatran.gtfsvtor.reporting.issues.InvalidCharsetError;
import com.mecatran.gtfsvtor.reporting.issues.InvalidEncodingError;
import com.mecatran.gtfsvtor.reporting.issues.MissingMandatoryColumnError;
import com.mecatran.gtfsvtor.reporting.issues.MissingMandatoryTableError;
import com.mecatran.gtfsvtor.reporting.issues.TableIOError;
import com.mecatran.gtfsvtor.reporting.issues.UnknownFileInfo;
import com.mecatran.gtfsvtor.reporting.issues.UnrecognizedColumnInfo;
import com.mecatran.gtfsvtor.validation.StreamingValidator;

public class GtfsDataLoader implements DataLoader {

	private NamedTabularDataSource dataSource;
	private boolean missingCalendarsTable = false;
	private boolean smallShapePoint = true;
	private boolean smallStopTime = false;

	public GtfsDataLoader(NamedTabularDataSource dataSource) {
		this.dataSource = dataSource;
	}

	public GtfsDataLoader withSmallShapePoint(boolean small) {
		this.smallShapePoint = small;
		return this;
	}

	public GtfsDataLoader withSmallStopTime(boolean small) {
		this.smallStopTime = small;
		return this;
	}

	@Override
	public void load(DataLoader.Context context) {
		/*
		 * Warning! The order below is important, as streaming validators rely
		 * on the partially loaded DAO to check for references, when possible.
		 * For exemple when loading a trip we check if the calendar or shape ID
		 * exists, if defined. Etc...
		 */
		loadFeedInfo(context);
		loadAgencies(context);
		// Routes references agencies
		loadRoutes(context);
		loadLevels(context);
		// Stops references levels
		loadStops(context);
		loadCalendars(context);
		loadCalendarDates(context);
		loadShapes(context);
		// Trips references routes, calendars and shapes
		loadTrips(context);
		// Stop times references trips, stops
		loadStopTimes(context);
		// Frequencies references trips
		loadFrequencies(context);
		// Transfers references stops
		loadTransfers(context);
		// Pathways references stops
		loadPathways(context);
		// Fare attributes references agencies
		loadFareAttributes(context);
		// Fare rule references fare attributes, routes, zones
		loadFareRules(context);
		reportUnreadTables(context.getReportSink());
		context.getDao().close();
	}

	private void loadFeedInfo(DataLoader.Context context) {
		DataTable table = getDataTable(GtfsFeedInfo.TABLE_NAME, false,
				context.getReportSink());
		if (table == null)
			return;
		checkMandatoryColumns(context.getReportSink(), table,
				"feed_publisher_name", "feed_publisher_url", "feed_lang");
		DataTableContext sourceContext = new DataTableContext(table,
				context.getReportSink(), context.getReadOnlyDao());
		for (DataRow row : table) {
			DataRowConverter erow = new DataRowConverter(row,
					context.getReportSink());
			GtfsFeedInfo.Builder builder = new GtfsFeedInfo.Builder();
			builder.withSourceLineNumber(table.getCurrentLineNumber())
					.withFeedPublisherName(
							erow.getString("feed_publisher_name"))
					.withFeedPublisherUrl(erow.getString("feed_publisher_url"))
					.withFeedLang(erow.getLocale("feed_lang", true))
					.withDefaultLang(erow.getLocale("default_lang", false))
					.withFeedStartDate(
							erow.getLogicalDate("feed_start_date", false))
					.withFeedEndDate(
							erow.getLogicalDate("feed_end_date", false))
					.withFeedVersion(erow.getString("feed_version"))
					.withFeedContactEmail(erow.getString("feed_contact_email"))
					.withFeedContactUrl(erow.getString("feed_contact_url"));
			GtfsFeedInfo feedInfo = builder.build();
			sourceContext.setAndValidateRow(row);
			context.getStreamingValidator().validate(GtfsFeedInfo.class,
					feedInfo, sourceContext);
			context.getDao().setFeedInfo(feedInfo, sourceContext);
		}
		closeTable(table, context.getReportSink());
	}

	private void loadAgencies(DataLoader.Context context) {
		DataTable table = getDataTable(GtfsAgency.TABLE_NAME, true,
				context.getReportSink());
		if (table == null)
			return;
		checkMandatoryColumns(context.getReportSink(), table, "agency_id",
				"agency_name", "agency_url", "agency_timezone");
		DataTableContext sourceContext = new DataTableContext(table,
				context.getReportSink(), context.getReadOnlyDao());
		for (DataRow row : table) {
			DataRowConverter erow = new DataRowConverter(row,
					context.getReportSink());
			GtfsAgency.Builder builder = new GtfsAgency.Builder(
					erow.getString("agency_id"));
			builder.withSourceLineNumber(table.getCurrentLineNumber())
					.withName(erow.getString("agency_name", true))
					.withUrl(erow.getString("agency_url", true))
					.withTimezone(erow.getTimeZone("agency_timezone", true))
					.withLang(erow.getLocale("agency_lang", false))
					.withPhone(erow.getString("agency_phone"))
					.withFareUrl(erow.getString("agency_fare_url"))
					.withEmail(erow.getString("agency_email"));
			GtfsAgency agency = builder.build();
			sourceContext.setAndValidateRow(row);
			context.getStreamingValidator().validate(GtfsAgency.class, agency,
					sourceContext);
			context.getDao().addAgency(agency, sourceContext);
		}
		closeTable(table, context.getReportSink());
	}

	private void loadRoutes(DataLoader.Context context) {
		DataTable table = getDataTable(GtfsRoute.TABLE_NAME, true,
				context.getReportSink());
		if (table == null)
			return;
		checkMandatoryColumns(context.getReportSink(), table, "route_id",
				"route_type");
		DataTableContext sourceContext = new DataTableContext(table,
				context.getReportSink(), context.getReadOnlyDao());
		for (DataRow row : table) {
			DataRowConverter erow = new DataRowConverter(row,
					context.getReportSink());
			GtfsRoute.Builder builder = new GtfsRoute.Builder(
					erow.getString("route_id"));
			builder.withSourceLineNumber(table.getCurrentLineNumber())
					.withAgencyId(GtfsAgency
							.id(erow.getString("agency_id", "", false)))
					.withType(GtfsRouteType
							.fromValue(erow.getInteger("route_type", true)))
					.withShortName(erow.getString("route_short_name"))
					.withLongName(erow.getString("route_long_name"))
					.withDescription(erow.getString("route_desc"))
					.withUrl(erow.getString("route_url"))
					.withColor(erow.getColor("route_color"))
					.withTextColor(erow.getColor("route_text_color"))
					.withSortOrder(erow.getInteger("route_sort_order", false));
			GtfsRoute route = builder.build();
			sourceContext.setAndValidateRow(row);
			context.getStreamingValidator().validate(GtfsRoute.class, route,
					sourceContext);
			context.getDao().addRoute(route, sourceContext);
		}
		closeTable(table, context.getReportSink());
	}

	private void loadLevels(DataLoader.Context context) {
		DataTable table = getDataTable(GtfsLevel.TABLE_NAME, false,
				context.getReportSink());
		if (table == null)
			return;
		checkMandatoryColumns(context.getReportSink(), table, "level_id",
				"level_index");
		DataTableContext sourceContext = new DataTableContext(table,
				context.getReportSink(), context.getReadOnlyDao());
		for (DataRow row : table) {
			DataRowConverter erow = new DataRowConverter(row,
					context.getReportSink());
			GtfsLevel.Builder builder = new GtfsLevel.Builder(
					erow.getString("level_id"));
			builder.withSourceLineNumber(table.getCurrentLineNumber())
					.withIndex(erow.getDouble("level_index", true))
					.withName(erow.getString("level_name"));
			GtfsLevel level = builder.build();
			sourceContext.setAndValidateRow(row);
			context.getStreamingValidator().validate(GtfsLevel.class, level,
					sourceContext);
			context.getDao().addLevel(level, sourceContext);
		}
		closeTable(table, context.getReportSink());
	}

	private void loadStops(DataLoader.Context context) {
		DataTable table = getDataTable(GtfsStop.TABLE_NAME, true,
				context.getReportSink());
		if (table == null)
			return;
		checkMandatoryColumns(context.getReportSink(), table, "stop_id",
				"stop_name", "stop_lat", "stop_lon");
		DataTableContext sourceContext = new DataTableContext(table,
				context.getReportSink(), context.getReadOnlyDao());
		for (DataRow row : table) {
			DataRowConverter erow = new DataRowConverter(row,
					context.getReportSink());
			GtfsStop.Builder builder = new GtfsStop.Builder(
					erow.getString("stop_id"));
			builder.withSourceLineNumber(table.getCurrentLineNumber())
					.withType(erow.getStopType("location_type"))
					.withCode(erow.getString("stop_code"))
					.withName(erow.getString("stop_name", true))
					.withCoordinates(
							erow.getDouble("stop_lat", null, Double.NaN, false),
							erow.getDouble("stop_lon", null, Double.NaN, false))
					.withParentId(GtfsStop.id(erow.getString("parent_station")))
					.withDescription(erow.getString("stop_desc"))
					.withZoneId(GtfsZone.id(erow.getString("zone_id")))
					.withUrl(erow.getString("stop_url"))
					.withTimezone(erow.getTimeZone("stop_timezone"))
					.withWheelchairBoarding(
							erow.getWheelchairAccess("wheelchair_boarding"))
					.withLevelId(GtfsLevel.id(erow.getString("level_id")))
					.withPlatformCode(erow.getString("platform_code"));
			GtfsStop stop = builder.build();
			sourceContext.setAndValidateRow(row);
			context.getStreamingValidator().validate(GtfsStop.class, stop,
					sourceContext);
			context.getDao().addStop(stop, sourceContext);
		}
		closeTable(table, context.getReportSink());
	}

	private void loadCalendars(DataLoader.Context context) {
		DataTable table = getDataTable(GtfsCalendar.TABLE_NAME, false,
				context.getReportSink());
		if (table == null) {
			missingCalendarsTable = true;
			return;
		}
		checkMandatoryColumns(context.getReportSink(), table, "service_id",
				"monday", "tuesday", "wednesday", "thursday", "friday",
				"saturday", "sunday", "start_date", "end_date");
		DataTableContext sourceContext = new DataTableContext(table,
				context.getReportSink(), context.getReadOnlyDao());
		for (DataRow row : table) {
			DataRowConverter erow = new DataRowConverter(row,
					context.getReportSink());
			GtfsCalendar.Builder builder = new GtfsCalendar.Builder(
					erow.getString("service_id"));
			builder.withSourceLineNumber(table.getCurrentLineNumber())
					.withDow(erow.getBoolean("monday", true),
							erow.getBoolean("tuesday", true),
							erow.getBoolean("wednesday", true),
							erow.getBoolean("thursday", true),
							erow.getBoolean("friday", true),
							erow.getBoolean("saturday", true),
							erow.getBoolean("sunday", true))
					.withStartDate(erow.getLogicalDate("start_date", true))
					.withEndDate(erow.getLogicalDate("end_date", true));
			GtfsCalendar calendar = builder.build();
			sourceContext.setAndValidateRow(row);
			context.getStreamingValidator().validate(GtfsCalendar.class,
					calendar, sourceContext);
			context.getDao().addCalendar(calendar, sourceContext);
		}
		closeTable(table, context.getReportSink());
	}

	private void loadCalendarDates(DataLoader.Context context) {
		DataTable table = getDataTable(GtfsCalendarDate.TABLE_NAME,
				missingCalendarsTable, context.getReportSink());
		if (table == null)
			return;
		checkMandatoryColumns(context.getReportSink(), table, "service_id",
				"date", "exception_type");
		DataTableContext sourceContext = new DataTableContext(table,
				context.getReportSink(), context.getReadOnlyDao());
		for (DataRow row : table) {
			DataRowConverter erow = new DataRowConverter(row,
					context.getReportSink());
			GtfsCalendarDate.Builder builder = new GtfsCalendarDate.Builder();
			builder.withSourceLineNumber(table.getCurrentLineNumber())
					.withCalendarId(
							GtfsCalendar.id(erow.getString("service_id")))
					.withDate(erow.getLogicalDate("date", true))
					.withExceptionType(erow
							.getCalendarDateExceptionType("exception_type"));
			GtfsCalendarDate calendarDate = builder.build();
			sourceContext.setAndValidateRow(row);
			context.getStreamingValidator().validate(GtfsCalendarDate.class,
					calendarDate, sourceContext);
			context.getDao().addCalendarDate(calendarDate, sourceContext);
		}
		closeTable(table, context.getReportSink());
	}

	private void loadShapes(DataLoader.Context context) {
		DataTable table = getDataTable(GtfsShapePoint.TABLE_NAME, false,
				context.getReportSink());
		if (table == null)
			return;
		checkMandatoryColumns(context.getReportSink(), table, "shape_id",
				"shape_pt_lat", "shape_pt_lon", "shape_pt_sequence");
		DataTableContext sourceContext = new DataTableContext(table,
				context.getReportSink(), context.getReadOnlyDao());
		int nShapePoints = 0;
		for (DataRow row : table) {
			DataRowConverter erow = new DataRowConverter(row,
					context.getReportSink());
			GtfsShapePoint.Builder builder = smallShapePoint
					? new SmallGtfsShapePoint.Builder()
					: new SimpleGtfsShapePoint.Builder();
			builder.withShapeId(GtfsShape.id(erow.getString("shape_id")))
					.withCoordinates(erow.getDouble("shape_pt_lat", true),
							erow.getDouble("shape_pt_lon", true))
					.withPointSequence(
							erow.getShapePointSequence("shape_pt_sequence"))
					.withShapeDistTraveled(
							erow.getDouble("shape_dist_traveled", false));
			GtfsShapePoint shapePoint = builder.build();
			sourceContext.setAndValidateRow(row);
			context.getStreamingValidator().validate(GtfsShapePoint.class,
					shapePoint, sourceContext);
			context.getDao().addShapePoint(shapePoint, sourceContext);
			nShapePoints++;
			if ((nShapePoints % 100000) == 0) {
				// TODO Fancier progress bar
				System.out.print("\r" + nShapePoints + " shape points   ");
			}
		}
		closeTable(table, context.getReportSink());
	}

	private void loadTrips(DataLoader.Context context) {
		DataTable table = getDataTable(GtfsTrip.TABLE_NAME, true,
				context.getReportSink());
		if (table == null)
			return;
		checkMandatoryColumns(context.getReportSink(), table, "route_id",
				"service_id", "trip_id");
		DataTableContext sourceContext = new DataTableContext(table,
				context.getReportSink(), context.getReadOnlyDao());
		for (DataRow row : table) {
			DataRowConverter erow = new DataRowConverter(row,
					context.getReportSink());
			GtfsTrip.Builder builder = new GtfsTrip.Builder(
					erow.getString("trip_id"));
			builder.withSourceLineNumber(table.getCurrentLineNumber())
					.withRouteId(GtfsRoute.id(erow.getString("route_id")))
					.withServiceId(
							GtfsCalendar.id(erow.getString("service_id")))
					.withHeadsign(erow.getString("trip_headsign"))
					.withShortName(erow.getString("trip_short_name"))
					.withBlockId(erow.getBlockId("block_id"))
					.withDirectionId(erow.getDirectionId("direction_id"))
					.withShapeId(GtfsShape.id(erow.getString("shape_id")))
					.withWheelchairAccessible(
							erow.getWheelchairAccess("wheelchair_accessible"))
					.withBikesAllowed(erow.getBikeAccess("bikes_allowed"));
			GtfsTrip trip = builder.build();
			sourceContext.setAndValidateRow(row);
			context.getStreamingValidator().validate(GtfsTrip.class, trip,
					sourceContext);
			context.getDao().addTrip(trip, sourceContext);
		}
		closeTable(table, context.getReportSink());
	}

	private void loadStopTimes(DataLoader.Context context) {
		long start = System.currentTimeMillis();
		DataTable table = getDataTable(GtfsStopTime.TABLE_NAME, true,
				context.getReportSink());
		if (table == null)
			return;
		checkMandatoryColumns(context.getReportSink(), table, "trip_id",
				"arrival_time", "departure_time", "stop_id", "stop_sequence");
		DataTableContext sourceContext = new DataTableContext(table,
				context.getReportSink(), context.getReadOnlyDao());
		int nStopTimes = 0;
		for (DataRow row : table) {
			DataRowConverter erow = new DataRowConverter(row,
					context.getReportSink());
			GtfsStopTime.Builder builder = smallStopTime
					? new SmallGtfsStopTime.Builder()
					: new SimpleGtfsStopTime.Builder();
			builder.withTripId(GtfsTrip.id(erow.getString("trip_id")))
					.withArrivalTime(erow.getLogicalTime("arrival_time", false))
					.withDepartureTime(
							erow.getLogicalTime("departure_time", false))
					.withStopId(GtfsStop.id(erow.getString("stop_id")))
					.withStopSequence(erow.getTripStopSequence("stop_sequence"))
					.withStopHeadsign(erow.getString("stop_headsign"))
					.withPickupType(erow.getPickupType("pickup_type"))
					.withDropoffType(erow.getDropoffType("drop_off_type"))
					.withShapeDistTraveled(
							erow.getDouble("shape_dist_traveled", false))
					.withTimepoint(erow.getTimepoint("timepoint"));
			GtfsStopTime stopTime = builder.build();
			sourceContext.setAndValidateRow(row);
			context.getStreamingValidator().validate(GtfsStopTime.class,
					stopTime, sourceContext);
			context.getDao().addStopTime(stopTime, sourceContext);
			nStopTimes++;
			if ((nStopTimes % 100000) == 0) {
				// TODO Fancier progress bar
				System.out.print("\r" + nStopTimes + " stop times   ");
			}
		}
		long end = System.currentTimeMillis();
		if (nStopTimes > 100000)
			System.out.println("\rLoaded " + nStopTimes + " stop times in "
					+ (end - start) + "ms");
		closeTable(table, context.getReportSink());
	}

	private void loadFrequencies(DataLoader.Context context) {
		DataTable table = getDataTable(GtfsFrequency.TABLE_NAME, false,
				context.getReportSink());
		if (table == null)
			return;
		checkMandatoryColumns(context.getReportSink(), table, "trip_id",
				"start_time", "end_time", "headway_secs");
		DataTableContext sourceContext = new DataTableContext(table,
				context.getReportSink(), context.getReadOnlyDao());
		for (DataRow row : table) {
			DataRowConverter erow = new DataRowConverter(row,
					context.getReportSink());
			GtfsFrequency.Builder builder = new GtfsFrequency.Builder();
			builder.withSourceLineNumber(table.getCurrentLineNumber())
					.withTripId(GtfsTrip.id(erow.getString("trip_id")))
					.withStartTime(erow.getLogicalTime("start_time", true))
					.withEndTime(erow.getLogicalTime("end_time", true))
					.withHeadwaySeconds(erow.getInteger("headway_secs", true))
					.withExactTimes(erow.getExactTimes("exact_times"));
			GtfsFrequency frequency = builder.build();
			sourceContext.setAndValidateRow(row);
			context.getStreamingValidator().validate(GtfsFrequency.class,
					frequency, sourceContext);
			context.getDao().addFrequency(frequency, sourceContext);
		}
		closeTable(table, context.getReportSink());
	}

	private void loadTransfers(DataLoader.Context context) {
		DataTable table = getDataTable(GtfsTransfer.TABLE_NAME, false,
				context.getReportSink());
		if (table == null)
			return;
		checkMandatoryColumns(context.getReportSink(), table, "from_stop_id",
				"to_stop_id", "transfer_type");
		DataTableContext sourceContext = new DataTableContext(table,
				context.getReportSink(), context.getReadOnlyDao());
		for (DataRow row : table) {
			DataRowConverter erow = new DataRowConverter(row,
					context.getReportSink());
			GtfsTransfer.Builder builder = new GtfsTransfer.Builder();
			builder.withFromStopId(GtfsStop.id(erow.getString("from_stop_id")))
					.withToStopId(GtfsStop.id(erow.getString("to_stop_id")))
					.withFromRouteId(
							GtfsRoute.id(erow.getString("from_route_id")))
					.withToRouteId(GtfsRoute.id(erow.getString("to_route_id")))
					.withFromTripId(GtfsTrip.id(erow.getString("from_trip_id")))
					.withToTripId(GtfsTrip.id(erow.getString("to_trip_id")))
					.withTransferType(erow.getTransferType("transfer_type"))
					.withMinTransferTime(
							erow.getInteger("min_transfer_time", false));
			GtfsTransfer transfer = builder.build();
			sourceContext.setAndValidateRow(row);
			context.getStreamingValidator().validate(GtfsTransfer.class,
					transfer, sourceContext);
			context.getDao().addTransfer(transfer, sourceContext);
		}
		closeTable(table, context.getReportSink());
	}

	private void loadPathways(DataLoader.Context context) {
		DataTable table = getDataTable(GtfsPathway.TABLE_NAME, false,
				context.getReportSink());
		if (table == null)
			return;
		checkMandatoryColumns(context.getReportSink(), table, "pathway_id",
				"from_stop_id", "to_stop_id", "pathway_mode",
				"is_bidirectional");
		DataTableContext sourceContext = new DataTableContext(table,
				context.getReportSink(), context.getReadOnlyDao());
		for (DataRow row : table) {
			DataRowConverter erow = new DataRowConverter(row,
					context.getReportSink());
			GtfsPathway.Builder builder = new GtfsPathway.Builder(
					erow.getString("pathway_id"));
			builder.withFromStopId(GtfsStop.id(erow.getString("from_stop_id")))
					.withToStopId(GtfsStop.id(erow.getString("to_stop_id")))
					.withPathwayMode(erow.getPathwayMode("pathway_mode"))
					.withBidirectional(
							erow.getDirectionality("is_bidirectional"))
					.withLength(erow.getDouble("length", false))
					.withTraversalTime(erow.getInteger("traversal_time", false))
					.withStairCount(erow.getInteger("stair_count", false))
					.withMaxSlope(erow.getDouble("max_slope", false))
					.withMinWitdth(erow.getDouble("min_width", false))
					.withSignpostedAs(erow.getString("signposted_as"))
					.withReversedSignpostedAs(
							erow.getString("reversed_signposted_as"));
			GtfsPathway pathway = builder.build();
			sourceContext.setAndValidateRow(row);
			context.getStreamingValidator().validate(GtfsPathway.class, pathway,
					sourceContext);
			context.getDao().addPathway(pathway, sourceContext);
		}
		closeTable(table, context.getReportSink());
	}

	private void loadFareAttributes(DataLoader.Context context) {
		DataTable table = getDataTable(GtfsFareAttribute.TABLE_NAME, false,
				context.getReportSink());
		if (table == null)
			return;
		checkMandatoryColumns(context.getReportSink(), table, "fare_id",
				"price", "currency_type", "payment_method", "transfers");
		DataTableContext sourceContext = new DataTableContext(table,
				context.getReportSink(), context.getReadOnlyDao());
		for (DataRow row : table) {
			DataRowConverter erow = new DataRowConverter(row,
					context.getReportSink());
			GtfsFareAttribute.Builder builder = new GtfsFareAttribute.Builder(
					erow.getString("fare_id"));
			builder.withPrice(erow.getDouble("price", true))
					.withCurrencyType(erow.getCurrency("currency_type"))
					.withSourceLineNumber(table.getCurrentLineNumber())
					.withPaymentMethod(erow.getPaymentMethod("payment_method"))
					.withTransfers(erow.getNumTransfers("transfers"))
					.withAgencyId(GtfsAgency.id(erow.getString("agency_id")))
					.withTransferDuration(
							erow.getInteger("transfer_duration", false));
			GtfsFareAttribute fareAttribute = builder.build();
			sourceContext.setAndValidateRow(row);
			context.getStreamingValidator().validate(GtfsFareAttribute.class,
					fareAttribute, sourceContext);
			context.getDao().addFareAttribute(fareAttribute, sourceContext);
		}
		closeTable(table, context.getReportSink());
	}

	private void loadFareRules(DataLoader.Context context) {
		DataTable table = getDataTable(GtfsFareRule.TABLE_NAME, false,
				context.getReportSink());
		if (table == null)
			return;
		checkMandatoryColumns(context.getReportSink(), table, "fare_id");
		DataTableContext sourceContext = new DataTableContext(table,
				context.getReportSink(), context.getReadOnlyDao());
		for (DataRow row : table) {
			DataRowConverter erow = new DataRowConverter(row,
					context.getReportSink());
			GtfsFareRule.Builder builder = new GtfsFareRule.Builder();
			builder.withFareId(GtfsFareAttribute.id(erow.getString("fare_id")))
					.withSourceLineNumber(table.getCurrentLineNumber())
					.withRouteId(GtfsRoute.id(erow.getString("route_id")))
					.withOriginId(GtfsZone.id(erow.getString("origin_id")))
					.withDestinationId(
							GtfsZone.id(erow.getString("destination_id")))
					.withContainsId(GtfsZone.id(erow.getString("contains_id")));
			GtfsFareRule fareRule = builder.build();
			sourceContext.setAndValidateRow(row);
			context.getStreamingValidator().validate(GtfsFareRule.class,
					fareRule, sourceContext);
			context.getDao().addFareRule(fareRule, sourceContext);
		}
		closeTable(table, context.getReportSink());
	}

	private DataTable getDataTable(String tableName, boolean mandatory,
			ReportSink reportSink) {
		try {
			DataTable table = dataSource.getDataTable(tableName);
			if (!table.getCharset().equals(StandardCharsets.UTF_8)) {
				reportSink.report(
						new InvalidCharsetError(tableName, table.getCharset()));
			}
			return table;
		} catch (IOException e) {
			if (mandatory) {
				reportSink.report(new MissingMandatoryTableError(tableName));
			}
			return null;
		}
	}

	private void closeTable(DataTable table, ReportSink reportSink) {
		if (table.isEmpty()) {
			reportSink.report(new EmptyTableError(
					table.getTableSourceInfo().getTableName()));
		}
		for (String unknownColumn : table.getUnreadColumnHeaders()) {
			reportSink.report(new UnrecognizedColumnInfo(
					new DataObjectSourceRef(table.getTableName(), 1L),
					unknownColumn));
			if (unknownColumn.chars().anyMatch(c -> c == 0xFFFD || c == 0)) {
				reportSink.report(new InvalidEncodingError(
						new DataObjectSourceRef(table.getTableName(), 1L),
						unknownColumn, unknownColumn));
			}
		}
		try {
			table.close();
		} catch (IOException e) {
			reportSink.report(
					new TableIOError(table.getTableSourceInfo().getTableName(),
							e.getLocalizedMessage()));
		}
	}

	private void reportUnreadTables(ReportSink reportSink) {
		for (String unreadTable : dataSource.getUnreadEntries()) {
			reportSink.report(new UnknownFileInfo(unreadTable));
		}
	}

	private void checkMandatoryColumns(ReportSink reportSink, DataTable table,
			String... columnHeaders) {
		Set<String> headerSet = new HashSet<>();
		for (String columnHeader : table.getColumnHeaders()) {
			if (headerSet.contains(columnHeader)) {
				reportSink
						.report(new DuplicatedColumnError(table.getSourceRef(),
								columnHeader), table.getSourceInfo());
			} else {
				headerSet.add(columnHeader);
			}
		}
		for (String columnHeader : columnHeaders) {
			if (!headerSet.contains(columnHeader)) {
				reportSink.report(
						new MissingMandatoryColumnError(table.getSourceRef(),
								columnHeader),
						table.getSourceInfo());
			}
		}
	}

	private static class DataTableContext
			implements DataLoader.SourceContext, StreamingValidator.Context {

		private DataTable dataTable;
		private DataRow row;
		private ReportSink reportSink;
		private ReadOnlyDao dao;

		private DataTableContext(DataTable dataTable, ReportSink reportSink,
				ReadOnlyDao dao) {
			this.dataTable = dataTable;
			this.reportSink = reportSink;
			this.dao = dao;
		}

		private void setAndValidateRow(DataRow row) {
			this.row = row;
			int numberOfHeaderColumns = dataTable.getColumnHeaders().size();
			if (row.getRecordCount() != numberOfHeaderColumns) {
				reportSink.report(new InconsistentNumberOfFieldsWarning(
						row.getSourceRef(), row.getRecordCount(),
						numberOfHeaderColumns));
			}
		}

		@Override
		public DataObjectSourceRef getSourceRef() {
			return row == null
					? new DataObjectSourceRef(dataTable.getTableName(), 1L)
					: row.getSourceRef();
		}

		@Override
		public DataObjectSourceInfo getSourceInfo() {
			return row == null ? dataTable.getSourceInfo()
					: row.getSourceInfo();
		}

		@Override
		public ReportSink getReportSink() {
			return reportSink;
		}

		@Override
		public ReadOnlyDao getPartialDao() {
			return dao;
		}
	}

}
