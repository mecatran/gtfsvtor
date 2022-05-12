package com.mecatran.gtfsvtor.reporting.html;

import java.text.MessageFormat;

import com.googlecode.jatl.MarkupUtils;
import com.mecatran.gtfsvtor.model.GtfsColor;
import com.mecatran.gtfsvtor.reporting.FormattingOptions;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;

public class HtmlIssueFormatter implements IssueFormatter {

	private FormattingOptions fmtOptions;
	private StringBuffer sb = new StringBuffer();

	public HtmlIssueFormatter(FormattingOptions fmtOptions) {
		this.fmtOptions = fmtOptions;
	}

	@Override
	public FormattingOptions getFormattingOptions() {
		return fmtOptions;
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

	@Override
	public String colors(GtfsColor color, GtfsColor textColor) {
		return String.format(
				"<span style='display:inline-block; padding-left:0.4em; padding-right:0.4em; color:%s; background-color:%s'>%s</span>",
				textColor.toHtmlString(), color.toHtmlString(), "TEXT");
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

	public static String format(FormattingOptions fmtOptions,
			ReportIssue issue) {
		HtmlIssueFormatter fmt = new HtmlIssueFormatter(fmtOptions);
		issue.format(fmt);
		return fmt.getHtmlResult();
	}
}
