package com.mecatran.gtfsvtor.loader.schema;

import java.util.Arrays;
import java.util.List;

public class DefaultGtfsTableSchema implements GtfsTableSchema {

	private List<GtfsTableDescriptor> tableDescriptors;

	public DefaultGtfsTableSchema() {
		// TODO Ability to configure list of tables to load?
		// TODO Table dependencies?
		tableDescriptors = Arrays.asList( //
				//
				new GtfsFeedInfoTableDescriptor(),
				//
				new GtfsAgencyTableDescriptor(), //
				// Route reference agencies
				new GtfsRouteTableDescriptor(),
				//
				new GtfsLevelTableDescriptor(),
				// Stop reference levels
				new GtfsStopTableDescriptor(),
				//
				new GtfsCalendarTableDescriptor(),
				//
				new GtfsCalendarDateTableDescriptor(),
				//
				new GtfsShapePointTableDescriptor(),
				// Trip reference routes, calendars, shapes
				new GtfsTripTableDescriptor(),
				// StopTime reference trips, stops
				new GtfsStopTimeTableDescriptor(),
				// Frequency reference trips
				new GtfsFrequencyTableDescriptor(),
				// Transfer reference stops, routes, trips
				new GtfsTransferTableDescriptor(),
				// Pathway reference stops
				new GtfsPathwayTableDescriptor(),
				// FareAttribute reference agencies
				new GtfsFareAttributeTableDescriptor(),
				// FareRule reference fare, routes, zones
				new GtfsFareRuleTableDescriptor(),
				// Translation reference a lot of other tables
				new GtfsTranslationTableDescriptor(),
				// Attribution reference agencies, routes, trips
				new GtfsAttributionTableDescriptor(),
				//
				new GtfsAreaTableDescriptor(),
				// reference areas, stops
				new GtfsStopAreaTableDescriptor(),
				//
				new GtfsFareProductTableDescriptor(),
				// reference networks, areas, fare products
				new GtfsFareLegRuleTableDescriptor(),
				// reference leg groups, fare products
				new GtfsFareTransferRuleTableDescriptor());
	}

	@Override
	public List<GtfsTableDescriptor> getTableDescriptors() {
		return tableDescriptors;
	}

}
