package com.mecatran.gtfsvtor.reporting;

import java.util.Locale;

import com.mecatran.gtfsvtor.geospatial.GeoBounds;
import com.mecatran.gtfsvtor.geospatial.GeoCoordinates;
import com.mecatran.gtfsvtor.model.GtfsColor;
import com.mecatran.gtfsvtor.model.GtfsId;
import com.mecatran.gtfsvtor.model.GtfsLogicalDate;
import com.mecatran.gtfsvtor.model.GtfsLogicalTime;
import com.mecatran.gtfsvtor.model.GtfsObject;

public interface IssueFormatter {

	public void text(String template, Object... arguments);

	/** Format an object ID */
	public default <U, V extends GtfsObject<U>> String id(GtfsId<U, V> id) {
		return id(id == null ? "(null)" : id.getInternalId().toString());
	}

	/** Format an object ID as a string */
	public String id(String id);

	/** Format a field name, a type, a table name... */
	public String pre(Object data);

	/** Format a variable, object field value, general message... */
	public String var(String name);

	/** Format two colors (either colors themselves or a sample in HTML) */
	public String colors(GtfsColor color, GtfsColor textColor);

	/** Format a time */
	public default String time(GtfsLogicalTime time) {
		if (time == null)
			return "-:--:--";
		return String.format("%d:%02d:%02d", time.getHour(), time.getMinute(),
				time.getSecond());
	}

	/** Format a date */
	public default String date(GtfsLogicalDate date) {
		if (date == null)
			return "----/--/--";
		return String.format("%04d/%02d/%02d", date.getYear(), date.getMonth(),
				date.getDay());
	}

	/** Format coordinates */
	public default String coordinates(GeoCoordinates p) {
		if (p == null)
			return "(null)";
		return String.format(Locale.US, "(%.6f,%.6f)", p.getLat(), p.getLon());
	}

	/** Format bounding box */
	public default String bounds(GeoBounds b) {
		return String.format(Locale.US, "(%.6f,%.6f,%.6f,%.6f)",
				b.getMin().getLat(), b.getMin().getLon(), b.getMax().getLat(),
				b.getMax().getLon());
	}

	/** Format a distance in meters */
	public default String distance(Double distanceMeters) {
		return distanceMeters == null ? "?"
				: String.format(Locale.US, "%.2fm", distanceMeters);
	}
}
