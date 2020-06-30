package com.mecatran.gtfsvtor.reporting.html;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;

import com.googlecode.jatl.Html;
import com.mecatran.gtfsvtor.loader.DataObjectSourceInfo;
import com.mecatran.gtfsvtor.reporting.ReportFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssueCategory;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.ReviewReport;
import com.mecatran.gtfsvtor.reporting.ReviewReport.IssueCount;
import com.mecatran.gtfsvtor.reporting.html.ClassifiedReviewReport.IssuesGroup;
import com.mecatran.gtfsvtor.reporting.html.ClassifiedReviewReport.IssuesSubGroup;
import com.mecatran.gtfsvtor.utils.Pair;
import com.mecatran.gtfsvtor.utils.SystemEnvironment;

public class HtmlReportFormatter implements ReportFormatter {

	private OutputStream outputStream;
	private Writer writer;
	private Html html;

	public HtmlReportFormatter(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	@Override
	public void format(ReviewReport report) throws IOException {
		writer = new OutputStreamWriter(outputStream);
		html = new Html(writer);
		ClassifiedReviewReport clsReport = new ClassifiedReviewReport(report);
		formatHeader();
		formatSummary(report);
		for (IssuesGroup group : clsReport.getGroups()
				.collect(Collectors.toList())) {
			formatGroup(report, group);
		}
		formatFooter();
		writer.close();
		outputStream.close();
	}

	private void formatGroup(ReviewReport report, IssuesGroup group)
			throws IOException {
		html.h2();
		html.text(group.getGroupName());
		for (Pair<ReportIssueSeverity, Integer> count : group
				.getSeverityCounters()) {
			html.span().classAttr("smaller");
			html.text(" - " + count.getSecond());
			html.span().classAttr("badge " + count.getFirst().toString())
					.text(count.getFirst().toString()).end();
			html.end(); // span
		}
		html.end(); // h2;

		if (group.isDisplayCategoryCounters()) {
			html.ul();
			for (Pair<ReportIssueCategory, Integer> count : group
					.getCategoryCounters()) {
				ReportIssueCategory category = count.getFirst();
				html.li();
				html.text("" + count.getSecond());
				html.span()
						.classAttr("smaller badge "
								+ category.getSeverity().toString())
						.text(category.getSeverity().toString()).end();
				html.text(category.getCategoryName());
				html.end(); // li
			}
			html.end(); // ul
		}
		for (IssuesSubGroup subCategory : group.getSubGroups()) {
			formatSubCategory(report, subCategory);
		}
	}

	private void formatSubCategory(ReviewReport report,
			IssuesSubGroup subCategory) throws IOException {
		html.div().classAttr("subcategory");
		if (!subCategory.getSourceRefs().isEmpty()) {
			formatSourceInfos(report, subCategory);
		}
		formatIssueList(subCategory.getIssues());
		html.end(); // div.subcategory
	}

	private void formatSourceInfos(ReviewReport report,
			IssuesSubGroup subCategory) throws IOException {
		String lastTableName = null;
		boolean inTable = false;
		for (int sourceRefIndex = 0; sourceRefIndex < subCategory
				.getSourceRefs().size(); sourceRefIndex++) {
			DataObjectSourceInfo sourceInfo = report.getSourceInfo(
					subCategory.getSourceRefs().get(sourceRefIndex));
			String tableName = sourceInfo.getTable().getTableName();
			if (!tableName.equals(lastTableName)) {
				// Output table header
				if (inTable)
					html.end(); // table
				inTable = true;
				html.table().classAttr("sourceinfo");
				if (lastTableName != null) {
					html.caption().text(tableName).end();
				}
				html.thead().tr();
				html.td().classAttr("linenr")
						.text(sourceInfo.getLineNumber() == 1 ? "L1" : "")
						.end();
				for (String header : sourceInfo.getTable().getHeaderColumns()) {
					html.td();
					if (sourceInfo.getLineNumber() == 1) {
						ReportIssueSeverity fieldSeverity = subCategory
								.getFieldSeverity(header);
						if (fieldSeverity != null)
							html.classAttr(fieldSeverity.toString());
					}
					html.text(header);
					html.end(); // td
				}
				html.end().end(); // thead/tr
			}
			lastTableName = tableName;
			if (sourceInfo.getLineNumber() != 1) {
				html.tr();
				html.td().classAttr("linenr")
						.text("L" + sourceInfo.getLineNumber()).end();
				List<String> headers = sourceInfo.getTable().getHeaderColumns();
				List<String> fields = sourceInfo.getFields();
				for (int i = 0; i < fields.size(); i++) {
					String fieldValue = fields.get(i);
					String header = i < headers.size() ? headers.get(i) : "-";
					html.td();
					ReportIssueSeverity fieldSeverity = subCategory
							.getFieldSeverity(sourceRefIndex, header);
					if (fieldSeverity != null)
						html.classAttr(fieldSeverity.toString());
					html.text(fieldValue);
					html.end(); // td
				}
				html.end(); // tr
			}
		}
		if (inTable)
			html.end(); // table
	}

	private void formatIssueList(List<ReportIssue> issues) throws IOException {
		html.table().classAttr("issuelist");
		for (ReportIssue issue : issues) {
			ReportIssueSeverity severity = issue.getSeverity();
			html.tr();
			html.td().classAttr("severity " + severity.toString())
					.text(severity.toString()).end();
			List<String> fieldNames = issue.getSourceRefs().stream()
					.flatMap(si -> si.getFieldNames().stream()).sorted()
					.distinct().collect(Collectors.toList());
			html.td().classAttr("fieldname").text(String.join(", ", fieldNames))
					.end();
			html.td().classAttr("issue").raw(HtmlIssueFormatter.format(issue))
					.end();
			html.end();// tr
		}
		html.end(); // table
	}

	private void formatHeader() throws IOException {
		writer.write("<!DOCTYPE html>\n");
		html.html().lang("en").head();
		html.meta().charset("UTF-8").end();
		html.start("title").text("GTFS validation report").end();
		html.style().type("text/css");
		StringWriter cssWriter = new StringWriter();
		cssWriter.append("\n");
		IOUtils.copy(this.getClass().getResourceAsStream("report.css"),
				cssWriter, StandardCharsets.UTF_8);
		StringWriter logoWriter = new StringWriter();
		IOUtils.copy(this.getClass().getResourceAsStream("gtfsvtor_logo.svg"),
				logoWriter, StandardCharsets.UTF_8);
		logoWriter.close();
		cssWriter.append(
				"\n.logo {\n\tbackground-image: url('data:image/svg+xml;base64,");
		String logoBase64 = Base64.getEncoder().encodeToString(
				logoWriter.toString().getBytes(StandardCharsets.UTF_8));
		cssWriter.append(logoBase64);
		cssWriter.append("');\n}\n");
		cssWriter.close();
		html.raw(cssWriter.toString());
		html.end(); // style
	}

	private void formatSummary(ReviewReport report) throws IOException {
		html.h1().classAttr("logo image").text("GTFS validation report");
		for (ReportIssueSeverity severity : ReportIssueSeverity.values()) {
			int totalCount = report.issuesCountOfSeverity(severity)
					.totalCount();
			if (totalCount > 0) {
				html.span().classAttr("xsmaller");
				html.text(" - " + totalCount + " ");
				html.span().classAttr("badge " + severity.toString())
						.text(severity.name()).end();
				html.end();
			}
		}
		html.end(); // h1

		html.ul();
		report.getCategories().forEach(category -> {
			ReportIssueSeverity severity = category.getSeverity();
			IssueCount count = report.issuesCountOfCategory(category);
			html.li();
			html.text(count.totalCount() + " ");
			html.span().classAttr("smaller badge " + severity.toString())
					.text(severity.toString()).end();
			html.text(category.getCategoryName());
			if (count.reportedCount() != count.totalCount()) {
				html.span().classAttr("comments").text(" (of which "
						+ count.reportedCount() + " are displayed)").end();
			}
			html.end(); // li
		});
		html.end(); // ul
	}

	private void formatFooter() throws IOException {
		html.hr();
		Date now = SystemEnvironment.now();
		Calendar cal = GregorianCalendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		html.p().classAttr("comments").text(String.format(
				"Validation done at %s by GTFSVTOR - Copyright (c) %d Mecatran",
				now, year)).end();
		html.end(); // html
	}

}
