package com.mecatran.gtfsvtor.reporting;

import com.mecatran.gtfsvtor.model.GtfsColor;
import com.mecatran.gtfsvtor.model.GtfsId;
import com.mecatran.gtfsvtor.model.GtfsLogicalDate;
import com.mecatran.gtfsvtor.model.GtfsLogicalTime;
import com.mecatran.gtfsvtor.model.GtfsObject;

public interface IssueFormatter {

	public void text(String template, Object... arguments);

	/** Format an object ID */
	public default <U extends Comparable<U>, V extends GtfsObject<U>> String id(
			GtfsId<U, V> id) {
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
		return String.format("%d:%02d:%02d", time.getHour(), time.getMinute(),
				time.getSecond());
	}

	/** Format a date */
	public default String date(GtfsLogicalDate date) {
		return String.format("%04d/%02d/%02d", date.getYear(), date.getMonth(),
				date.getDay());
	}
}
