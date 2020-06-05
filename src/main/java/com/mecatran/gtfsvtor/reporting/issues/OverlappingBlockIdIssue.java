package com.mecatran.gtfsvtor.reporting.issues;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.mecatran.gtfsvtor.dao.CalendarIndex.OverlappingCalendarInfo;
import com.mecatran.gtfsvtor.model.GtfsBlockId;
import com.mecatran.gtfsvtor.model.GtfsLogicalTime;
import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.SourceRefWithFields;

@ReportIssuePolicy(severity = ReportIssueSeverity.WARNING, categoryName = "Overlapping trip in block")
public class OverlappingBlockIdIssue implements ReportIssue {

	private GtfsBlockId blockId;
	private GtfsTrip trip1, trip2;
	private GtfsLogicalTime trip1Start, trip1End, trip2Start, trip2End;
	private OverlappingCalendarInfo calendarOverlap;
	private List<SourceRefWithFields> sourceInfos;

	public OverlappingBlockIdIssue(GtfsBlockId blockId, GtfsTrip trip1,
			GtfsTrip trip2, GtfsLogicalTime trip1Start,
			GtfsLogicalTime trip1End, GtfsLogicalTime trip2Start,
			GtfsLogicalTime trip2End, OverlappingCalendarInfo calendarOverlap) {
		this.blockId = blockId;
		this.trip1 = trip1;
		this.trip2 = trip2;
		this.trip1Start = trip1Start;
		this.trip1End = trip1End;
		this.trip2Start = trip2Start;
		this.trip2End = trip2End;
		this.calendarOverlap = calendarOverlap;
		this.sourceInfos = Arrays.asList(
				new SourceRefWithFields(trip1.getSourceRef(), "trip_id",
						"block_id"),
				new SourceRefWithFields(trip2.getSourceRef(), "trip_id",
						"block_id"));
		Collections.sort(this.sourceInfos);
	}

	public GtfsBlockId getBlockId() {
		return blockId;
	}

	public GtfsTrip getTrip1() {
		return trip1;
	}

	public GtfsTrip getTrip2() {
		return trip2;
	}

	public OverlappingCalendarInfo getCalendarOverlap() {
		return calendarOverlap;
	}

	@Override
	public List<SourceRefWithFields> getSourceRefs() {
		return sourceInfos;
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text(
				"Overlapping trips on same block ID {0}: {1} ({2}→{3}) and {4} ({5}→{6}). Overlap on {7} days, from {8} to {9}.",
				fmt.id(blockId.getValue()), fmt.id(trip1.getId()),
				fmt.time(trip1Start), fmt.time(trip1End), fmt.id(trip2.getId()),
				fmt.time(trip2Start), fmt.time(trip2End),
				fmt.var(Integer.toString(calendarOverlap.getDaysCount())),
				fmt.var(fmt.date(calendarOverlap.getFrom())),
				fmt.var(fmt.date(calendarOverlap.getTo())));
	}
}
