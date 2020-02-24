package com.mecatran.gtfsvtor.reporting.impl;

import java.text.MessageFormat;

import com.mecatran.gtfsvtor.model.GtfsColor;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;

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
		return fmt.getPlainTextResult();
	}
}
