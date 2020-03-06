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
import com.mecatran.gtfsvtor.model.GtfsAgency;
import com.mecatran.gtfsvtor.model.GtfsCalendar;
import com.mecatran.gtfsvtor.model.GtfsCalendarDate;
import com.mecatran.gtfsvtor.model.GtfsRoute;
import com.mecatran.gtfsvtor.model.GtfsRouteType;
import com.mecatran.gtfsvtor.model.GtfsShape;
import com.mecatran.gtfsvtor.model.GtfsShapePoint;
import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.model.GtfsStopTime;
import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.model.GtfsZone;
import com.mecatran.gtfsvtor.model.impl.SimpleGtfsStopTime;
import com.mecatran.gtfsvtor.model.impl.SmallGtfsStopTime;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.DuplicatedColumnError;
import com.mecatran.gtfsvtor.reporting.issues.EmptyTableError;
import com.mecatran.gtfsvtor.reporting.issues.InvalidCharsetError;
import com.mecatran.gtfsvtor.reporting.issues.InvalidEncodingError;
import com.mecatran.gtfsvtor.reporting.issues.MissingMandatoryColumnError;
import com.mecatran.gtfsvtor.reporting.issues.MissingMandatoryTableError;
import com.mecatran.gtfsvtor.reporting.issues.TableIOError;
import com.mecatran.gtfsvtor.reporting.issues.UnknownFileInfo;
import com.mecatran.gtfsvtor.reporting.issues.UnrecognizedColumnInfo;
import com.mecatran.gtfsvtor.validation.StreamingValidator;

public class GtfsDataLoader implements DataLoader {

	// TODO Make this configurable
	private static final boolean OPTIMIZE_FOR_SIZE = true;

	private NamedTabularDataSource dataSource;
	private boolean missingCalendarsTable = false;

	public GtfsDataLoader(NamedTabularDataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public void load(DataLoader.Context context) {
		/*
		 * Warning! The order below is important, as streaming validators rely
		 * on the partially loaded DAO to check for references, when possible.
		 * For exemple when loading a trip we check if the calendar or shape ID
		 * exists, if defined. Etc...
		 */
		loadAgencies(context);
		// Routes references agencies
		loadRoutes(context);
		// Stops references levels
		loadStops(context);
		loadCalendars(context);
		loadCalendarDates(context);
		loadShapes(context);
		// Trips references routes, calendars and shapes
		loadTrips(context);
		// Stop times references trips, stops
		loadStopTimes(context);
		reportUnreadTables(context.getReportSink());
		context.getDao().close();
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
			builder.withSourceInfo(row.getSourceInfo())
					.withName(erow.getString("agency_name", true))
					.withUrl(erow.getString("agency_url", true))
					.withTimezone(erow.getTimeZone("agency_timezone", true))
					.withLang(erow.getLocale("agency_lang"))
					.withPhone(erow.getString("agency_phone"))
					.withFareUrl(erow.getString("agency_fare_url"))
					.withEmail(erow.getString("agency_email"));
			GtfsAgency agency = builder.build();
			sourceContext.setRow(row);
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
		DataTableContext sourceContext = new DataTableContext(table,
				context.getReportSink(), context.getReadOnlyDao());
		for (DataRow row : table) {
			DataRowConverter erow = new DataRowConverter(row,
					context.getReportSink());
			GtfsRoute.Builder builder = new GtfsRoute.Builder(
					erow.getString("route_id"));
			builder.withSourceInfo(row.getSourceInfo())
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
					.withSortOrder(erow.getInteger("route_sort_order"));
			GtfsRoute route = builder.build();
			sourceContext.setRow(row);
			context.getStreamingValidator().validate(GtfsRoute.class, route,
					sourceContext);
			context.getDao().addRoute(route, sourceContext);
		}
		closeTable(table, context.getReportSink());
	}

	private void loadStops(DataLoader.Context context) {
		DataTable table = getDataTable(GtfsStop.TABLE_NAME, true,
				context.getReportSink());
		if (table == null)
			return;
		DataTableContext sourceContext = new DataTableContext(table,
				context.getReportSink(), context.getReadOnlyDao());
		for (DataRow row : table) {
			DataRowConverter erow = new DataRowConverter(row,
					context.getReportSink());
			GtfsStop.Builder builder = new GtfsStop.Builder(
					erow.getString("stop_id"));
			builder.withSourceInfo(row.getSourceInfo())
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
					.withPlatformCode(erow.getString("platform_code"));
			GtfsStop stop = builder.build();
			sourceContext.setRow(row);
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
		DataTableContext sourceContext = new DataTableContext(table,
				context.getReportSink(), context.getReadOnlyDao());
		for (DataRow row : table) {
			DataRowConverter erow = new DataRowConverter(row,
					context.getReportSink());
			GtfsCalendar.Builder builder = new GtfsCalendar.Builder(
					erow.getString("service_id"));
			builder.withSourceInfo(row.getSourceInfo())
					.withDow(erow.getBoolean("monday", null, true),
							erow.getBoolean("tuesday", null, true),
							erow.getBoolean("wednesday", null, true),
							erow.getBoolean("thursday", null, true),
							erow.getBoolean("friday", null, true),
							erow.getBoolean("saturday", null, true),
							erow.getBoolean("sunday", null, true))
					.withStartDate(erow.getLogicalDate("start_date", true))
					.withEndDate(erow.getLogicalDate("end_date", true));
			GtfsCalendar calendar = builder.build();
			sourceContext.setRow(row);
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
		DataTableContext sourceContext = new DataTableContext(table,
				context.getReportSink(), context.getReadOnlyDao());
		for (DataRow row : table) {
			DataRowConverter erow = new DataRowConverter(row,
					context.getReportSink());
			GtfsCalendarDate.Builder builder = new GtfsCalendarDate.Builder();
			builder.withSourceInfo(row.getSourceInfo())
					.withCalendarId(
							GtfsCalendar.id(erow.getString("service_id")))
					.withDate(erow.getLogicalDate("date", true))
					.withExceptionType(erow
							.getCalendarDateExceptionType("exception_type"));
			GtfsCalendarDate calendarDate = builder.build();
			sourceContext.setRow(row);
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
		DataTableContext sourceContext = new DataTableContext(table,
				context.getReportSink(), context.getReadOnlyDao());
		for (DataRow row : table) {
			DataRowConverter erow = new DataRowConverter(row,
					context.getReportSink());
			GtfsShapePoint.Builder builder = new GtfsShapePoint.Builder();
			builder.withShapeId(GtfsShape.id(erow.getString("shape_id")))
					.withCoordinates(erow.getDouble("shape_pt_lat", true),
							erow.getDouble("shape_pt_lon", true))
					.withPointSequence(
							erow.getShapePointSequence("shape_pt_sequence"))
					.withShapeDistTraveled(
							erow.getDouble("shape_dist_traveled"));
			GtfsShapePoint shapePoint = builder.build();
			sourceContext.setRow(row);
			context.getStreamingValidator().validate(GtfsShapePoint.class,
					shapePoint, sourceContext);
			context.getDao().addShapePoint(shapePoint, sourceContext);
		}
		closeTable(table, context.getReportSink());
	}

	private void loadTrips(DataLoader.Context context) {
		DataTable table = getDataTable(GtfsTrip.TABLE_NAME, true,
				context.getReportSink());
		if (table == null)
			return;
		DataTableContext sourceContext = new DataTableContext(table,
				context.getReportSink(), context.getReadOnlyDao());
		for (DataRow row : table) {
			DataRowConverter erow = new DataRowConverter(row,
					context.getReportSink());
			GtfsTrip.Builder builder = new GtfsTrip.Builder(
					erow.getString("trip_id"));
			builder.withSourceInfo(row.getSourceInfo())
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
			sourceContext.setRow(row);
			context.getStreamingValidator().validate(GtfsTrip.class, trip,
					sourceContext);
			context.getDao().addTrip(trip, sourceContext);
		}
		closeTable(table, context.getReportSink());
	}

	private void loadStopTimes(DataLoader.Context context) {
		DataTable table = getDataTable(GtfsStopTime.TABLE_NAME, true,
				context.getReportSink());
		if (table == null)
			return;
		DataTableContext sourceContext = new DataTableContext(table,
				context.getReportSink(), context.getReadOnlyDao());
		int nStopTimes = 0;
		for (DataRow row : table) {
			DataRowConverter erow = new DataRowConverter(row,
					context.getReportSink());
			GtfsStopTime.Builder builder = OPTIMIZE_FOR_SIZE
					? new SmallGtfsStopTime.Builder()
					: new SimpleGtfsStopTime.Builder();
			builder.withTripId(GtfsTrip.id(erow.getString("trip_id")))
					.withArrivalTime(erow.getLogicalTime("arrival_time"))
					.withDepartureTime(erow.getLogicalTime("departure_time"))
					.withStopId(GtfsStop.id(erow.getString("stop_id")))
					.withStopSequence(erow.getTripStopSequence("stop_sequence"))
					.withStopHeadsign(erow.getString("stop_headsign"))
					.withPickupType(erow.getPickupType("pickup_type"))
					.withDropoffType(erow.getDropoffType("drop_off_type"))
					.withShapeDistTraveled(
							erow.getDouble("shape_dist_traveled"))
					.withTimepoint(erow.getTimepoint("timepoint"));
			GtfsStopTime stopTime = builder.build();
			sourceContext.setRow(row);
			context.getStreamingValidator().validate(GtfsStopTime.class,
					stopTime, sourceContext);
			context.getDao().addStopTime(stopTime, sourceContext);
			nStopTimes++;
			if ((nStopTimes % 100000) == 0) {
				// TODO Fancier progress bar
				System.out.print("\r" + nStopTimes);
			}
		}
		if (nStopTimes > 100000)
			System.out.println("\rLoaded " + nStopTimes + " stop times.");
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
					new DataObjectSourceInfoImpl(table.getTableSourceInfo()),
					unknownColumn));
			if (unknownColumn.contains("\uFFFD")) {
				reportSink.report(new InvalidEncodingError(
						new DataObjectSourceInfoImpl(
								table.getTableSourceInfo()),
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
						.report(new DuplicatedColumnError(
								new DataObjectSourceInfoImpl(
										table.getTableSourceInfo()),
								columnHeader));
			} else {
				headerSet.add(columnHeader);
			}
		}
		for (String columnHeader : columnHeaders) {
			if (!headerSet.contains(columnHeader)) {
				reportSink
						.report(new MissingMandatoryColumnError(
								new DataObjectSourceInfoImpl(
										table.getTableSourceInfo()),
								columnHeader));
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

		private void setRow(DataRow row) {
			this.row = row;
		}

		@Override
		public DataObjectSourceInfo getSourceInfo() {
			return row == null
					? new DataObjectSourceInfoImpl(
							dataTable.getTableSourceInfo())
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
