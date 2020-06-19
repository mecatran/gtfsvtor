package com.mecatran.gtfsvtor.dao;

import com.mecatran.gtfsvtor.loader.DataObjectSourceInfo;
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
import com.mecatran.gtfsvtor.model.GtfsShapePoint;
import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.model.GtfsStopTime;
import com.mecatran.gtfsvtor.model.GtfsTransfer;
import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.reporting.ReportSink;

public interface AppendableDao {

	public interface SourceContext {

		public ReportSink getReportSink();

		public DataObjectSourceRef getSourceRef();

		public DataObjectSourceInfo getSourceInfo();
	}

	public void setFeedInfo(GtfsFeedInfo feedInfo, SourceContext sourceContext);

	public void addAgency(GtfsAgency agency, SourceContext sourceContext);

	public void addRoute(GtfsRoute route, SourceContext sourceContext);

	public void addStop(GtfsStop stop, SourceContext sourceContext);

	public void addCalendar(GtfsCalendar calendar, SourceContext sourceContext);

	public void addCalendarDate(GtfsCalendarDate calendarDate,
			SourceContext sourceContext);

	public void addShapePoint(GtfsShapePoint shapePoint,
			SourceContext sourceContext);

	public void addTrip(GtfsTrip trip, SourceContext sourceContext);

	public void addStopTime(GtfsStopTime stopTime, SourceContext sourceContext);

	public void addFrequency(GtfsFrequency frequency,
			SourceContext sourceContext);

	public void addTransfer(GtfsTransfer transfer, SourceContext sourceContext);

	public void addPathway(GtfsPathway pathway, SourceContext sourceContext);

	public void addFareAttribute(GtfsFareAttribute fareAttribute,
			SourceContext sourceContext);

	public void addFareRule(GtfsFareRule fareRule, SourceContext sourceContext);

	public void addLevel(GtfsLevel level, SourceContext sourceContext);

	public void close();
}
