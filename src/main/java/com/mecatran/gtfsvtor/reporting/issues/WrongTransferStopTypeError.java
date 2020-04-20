package com.mecatran.gtfsvtor.reporting.issues;

import java.util.Arrays;
import java.util.List;

import com.mecatran.gtfsvtor.loader.DataObjectSourceInfo;
import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.model.GtfsStopType;
import com.mecatran.gtfsvtor.model.GtfsTransfer;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.SourceInfoWithFields;

@ReportIssuePolicy(severity = ReportIssueSeverity.ERROR, categoryName = "Wrong transfer stop type")
public class WrongTransferStopTypeError implements ReportIssue {

	private GtfsTransfer transfer;
	private GtfsStop stop;
	private String fieldname;
	private SourceInfoWithFields sourceInfo;

	public WrongTransferStopTypeError(DataObjectSourceInfo sourceInfo,
			GtfsTransfer transfer, GtfsStop stop, String fieldname) {
		this.transfer = transfer;
		this.stop = stop;
		this.fieldname = fieldname;
		this.sourceInfo = new SourceInfoWithFields(sourceInfo, fieldname);
	}

	public GtfsTransfer getTransfer() {
		return transfer;
	}

	public GtfsStop getStop() {
		return stop;
	}

	public String getFieldname() {
		return fieldname;
	}

	@Override
	public List<SourceInfoWithFields> getSourceInfos() {
		return Arrays.asList(sourceInfo);
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text("Wrong type {0} for {1} {2} {3}, should be type {4} or {5}",
				fmt.pre(stop.getType().toString()), fmt.pre(fieldname),
				fmt.id(stop.getId()), fmt.var(stop.getName()),
				fmt.pre(GtfsStopType.STOP), fmt.pre(GtfsStopType.STATION));
	}
}
