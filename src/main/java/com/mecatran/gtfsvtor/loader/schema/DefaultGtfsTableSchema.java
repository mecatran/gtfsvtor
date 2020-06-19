package com.mecatran.gtfsvtor.loader.schema;

import java.util.Arrays;
import java.util.List;

public class DefaultGtfsTableSchema implements GtfsTableSchema {

	private List<GtfsTableDescriptor> tableDescriptors;

	public DefaultGtfsTableSchema() {
		// TODO Ability to configure list of tables to load
		// TODO Table dependencies
		tableDescriptors = Arrays.asList( //
				new GtfsFeedInfoTableDescriptor(), //
				new GtfsAgencyTableDescriptor(), //
				new GtfsRouteTableDescriptor(), // ref agencies
				new GtfsLevelTableDescriptor(), //
				new GtfsStopTableDescriptor(), // ref levels
				new GtfsCalendarTableDescriptor(), //
				new GtfsCalendarDateTableDescriptor(), //
				new GtfsShapePointTableDescriptor(), //
				new GtfsTripTableDescriptor(), // ref routes, calendars, shapes
				new GtfsStopTimeTableDescriptor(), // ref trips, stops
				new GtfsFrequencyTableDescriptor(), // ref trips
				new GtfsTransferTableDescriptor(), // ref stops, routes, trips
				new GtfsPathwayTableDescriptor(), // ref stops
				new GtfsFareAttributeTableDescriptor(), // ref agencies
				new GtfsFareRuleTableDescriptor() // ref fare, routes, zones
		);
	}

	@Override
	public List<GtfsTableDescriptor> getTableDescriptors() {
		return tableDescriptors;
	}

}
