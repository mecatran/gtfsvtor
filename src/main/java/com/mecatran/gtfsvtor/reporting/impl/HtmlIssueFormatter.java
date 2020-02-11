package com.mecatran.gtfsvtor.reporting.impl;

import java.text.MessageFormat;

import com.googlecode.jatl.MarkupUtils;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;

public class HtmlIssueFormatter implements IssueFormatter {

	private StringBuffer sb = new StringBuffer();

	public HtmlIssueFormatter() {
	}

	@Override
	public void text(String template, Object... arguments) {
		sb.append(MessageFormat.format(template, arguments));
	}

	@Override
	public String id(String id) {
		return span("id", id);
	}

	@Override
	public String pre(Object data) {
		return span("pre", data);
	}

	@Override
	public String var(String name) {
		return span("var", name);
	}

	private String span(String cssClass, Object data) {
		return "<span class='" + cssClass + "'>"
				+ MarkupUtils.escapeElementEntities(
						data == null ? "(null)" : data.toString())
				+ "</span>";
	}

	public String getHtmlResult() {
		return sb.toString();
	}

	public static String format(ReportIssue issue) {
		HtmlIssueFormatter fmt = new HtmlIssueFormatter();
		issue.format(fmt);
		return fmt.getHtmlResult();
	}
}
