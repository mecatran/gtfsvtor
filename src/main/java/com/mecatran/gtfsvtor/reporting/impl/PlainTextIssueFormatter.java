package com.mecatran.gtfsvtor.reporting.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mecatran.gtfsvtor.loader.DataObjectSourceInfo;
import com.mecatran.gtfsvtor.loader.TableSourceInfo;
import com.mecatran.gtfsvtor.model.GtfsColor;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.SourceInfoWithFields;

public class PlainTextIssueFormatter implements IssueFormatter {

	private StringBuffer sb = new StringBuffer();

	public PlainTextIssueFormatter() {
	}

	@Override
	public void text(String template, Object... arguments) {
		sb.append(MessageFormat.format(template, arguments));
	}

	@Override
	public String id(String id) {
		return "[" + id + "]";
	}

	@Override
	public String pre(Object data) {
		return data == null ? "(null)" : data.toString();
	}

	@Override
	public String var(String var) {
		return "`" + var + "'";
	}

	@Override
	public String colors(GtfsColor color, GtfsColor textColor) {
		return color.toHtmlString() + "/" + textColor.toHtmlString();
	}

	public String getPlainTextResult() {
		return sb.toString();
	}

	public static String format(ReportIssue issue) {
		// TODO Add source context infos
		PlainTextIssueFormatter fmt = new PlainTextIssueFormatter();
		issue.format(fmt);
		StringBuffer sb = new StringBuffer();
		for (SourceInfoWithFields siwf : issue.getSourceInfos()) {
			DataObjectSourceInfo dosi = siwf.getSourceInfo();
			TableSourceInfo tsi = dosi.getTable();
			sb.append(tsi.getTableName());
			sb.append(", line ").append(dosi.getLineNumber());
			sb.append(": ");
			List<String> fieldNames = new ArrayList<>(siwf.getFieldNames());
			Collections.sort(fieldNames);
			sb.append(String.join(", ", fieldNames));
			sb.append("\n");
		}
		sb.append(issue.getSeverity()).append(": ");
		sb.append(fmt.getPlainTextResult());
		return sb.toString();
	}
}
