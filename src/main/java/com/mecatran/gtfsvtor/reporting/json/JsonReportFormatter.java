package com.mecatran.gtfsvtor.reporting.json;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mecatran.gtfsvtor.cmdline.GtfsVtorMain;
import com.mecatran.gtfsvtor.cmdline.ManifestReader;
import com.mecatran.gtfsvtor.reporting.ReportFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.ReviewReport;
import com.mecatran.gtfsvtor.reporting.ReviewReport.IssueCount;
import com.mecatran.gtfsvtor.reporting.json.model.JsonReport;
import com.mecatran.gtfsvtor.reporting.json.model.JsonReport.JsonValidationRun;
import com.mecatran.gtfsvtor.utils.SystemEnvironment;

public class JsonReportFormatter implements ReportFormatter {

	private OutputStream outputStream;

	public JsonReportFormatter(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	@Override
	public void format(ReviewReport report) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		// TODO Implement append mode: read previous values
		JsonReport jreport = new JsonReport();
		JsonValidationRun run = convert(report);
		jreport.reports.add(run);
		mapper.writeValue(outputStream, jreport);
		outputStream.close();
	}

	private JsonValidationRun convert(ReviewReport report) {
		ManifestReader mfr = new ManifestReader(GtfsVtorMain.class);
		JsonValidationRun run = new JsonValidationRun();
		run.timestamp = SystemEnvironment.now();
		run.validator = "GTFSVTOR";
		run.validatorVersion = mfr.getApplicationVersion();
		run.validatorBuildDate = mfr.getApplicationBuildDate();
		run.validatorBuildRev = mfr.getApplicationBuildRevision();
		run.copyrights = "Copyright (c) Mecatran";
		// TODO Provide input data name
		run.inputDataName = "?";

		run.summary = new JsonReport.JsonSummary();
		run.summary.severities = Arrays.stream(ReportIssueSeverity.values())
				.map(severity -> {
					IssueCount count = report.issuesCountOfSeverity(severity);
					JsonReport.JsonSeverityCount jcount = new JsonReport.JsonSeverityCount();
					jcount.severity = severity.toString();
					jcount.totalCount = count.totalCount();
					// TODO use 0 for summary, reportedCount() for a whole
					// report
					jcount.reportedCount = 0;
					return jcount;
				}).collect(Collectors.toList());
		run.summary.categories = report.getCategories().sorted()
				.map(category -> {
					IssueCount count = report.issuesCountOfCategory(category);
					JsonReport.JsonCategoryCount jcount = new JsonReport.JsonCategoryCount();
					jcount.severity = category.getSeverity().toString();
					jcount.categoryName = category.getCategoryName();
					jcount.totalCount = count.totalCount();
					// TODO use 0 for summary, reportedCount() for a whole
					// report
					jcount.reportedCount = 0;
					return jcount;
				}).collect(Collectors.toList());
		return run;
	}
}
