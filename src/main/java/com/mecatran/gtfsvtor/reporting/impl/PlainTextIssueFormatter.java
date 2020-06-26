package com.mecatran.gtfsvtor.reporting.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mecatran.gtfsvtor.loader.DataObjectSourceInfo;
import com.mecatran.gtfsvtor.loader.TableSourceInfo;
import com.mecatran.gtfsvtor.model.GtfsColor;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.SourceInfoFactory;
import com.mecatran.gtfsvtor.reporting.SourceRefWithFields;
import com.mecatran.gtfsvtor.utils.MiscUtils;

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

	public static String format(Optional<SourceInfoFactory> sourceInfoFactory,
			ReportIssue issue) {
		PlainTextIssueFormatter fmt = new PlainTextIssueFormatter();
		issue.format(fmt);
		StringBuffer sb = new StringBuffer();
		sb.append("\n");
		/*
		 * As we have some source info loading post-poned (dao validators), we
		 * cannot use the code below. If we have it, print it, otherwise only
		 * print the reference. TODO Remove the code? It is currently not
		 * usable.
		 */
		if (sourceInfoFactory.isPresent()) {
			for (SourceRefWithFields siwf : issue.getSourceRefs()) {
				DataObjectSourceInfo dosi = sourceInfoFactory.get()
						.getSourceInfo(siwf.getSourceRef()).get();
				TableSourceInfo tsi = dosi.getTable();
				int nCols = tsi.getHeaderColumns().size();
				List<String> fields = dosi.getFields();
				int nVals = fields == null ? 0 : fields.size();
				int tbLen = tsi.getTableName().length();
				List<Integer> colWidths = new ArrayList<>();
				for (int i = 0; i < nCols; i++) {
					int headerWidth = tsi.getHeaderColumns().get(i).length();
					int valueWidth = 0;
					if (i < nVals) {
						String val = fields.get(i);
						valueWidth = val == null ? 0 : val.length();
					}
					colWidths.add(Math.max(headerWidth, valueWidth));
				}
				sb.append(pad(dosi.getTable().getTableName(), tbLen + 1, ' '));
				for (int i = 0; i < nCols; i++) {
					sb.append("| ").append(pad(tsi.getHeaderColumns().get(i),
							colWidths.get(i), ' ')).append(" ");
				}
				sb.append("|\n");
				sb.append(pad("", tbLen + 1, ' '));
				for (int i = 0; i < nCols; i++) {
					boolean highlight = siwf.getFieldNames()
							.contains(tsi.getHeaderColumns().get(i));
					sb.append(highlight ? "+=" : "+-")
							.append(pad("", colWidths.get(i),
									highlight ? '=' : '-'))
							.append(highlight ? "=" : "-");
				}
				sb.append("+\n");
				if (nVals > 0) {
					sb.append(pad(String.format("L%d", dosi.getLineNumber()),
							tbLen + 1, ' '));
					for (int i = 0; i < nCols && i < nVals; i++) {
						String val = fields.get(i);
						sb.append("| ").append(pad(val == null ? "" : val,
								colWidths.get(i), ' ')).append(" ");
					}
					sb.append("|\n");
					sb.append(pad("", tbLen + 1, ' '));
					for (int i = 0; i < nCols; i++) {
						boolean highlight = siwf.getFieldNames()
								.contains(tsi.getHeaderColumns().get(i));
						sb.append(highlight ? "+=" : "+-")
								.append(pad("", colWidths.get(i),
										highlight ? '=' : '-'))
								.append(highlight ? "=" : "-");
					}
					sb.append("+\n");
				}
			}
		} else {
			issue.getSourceRefs()
					.forEach(refwf -> sb.append(refwf.toString()).append("\n"));
		}
		String severity = issue.getSeverity().toString();
		List<String> lines = MiscUtils
				.wordProcessorSplit(fmt.getPlainTextResult(), 70);
		for (int i = 0; i < lines.size(); i++) {
			if (i == 0) {
				sb.append(severity).append(" | ");
			} else {
				sb.append(pad("", severity.length(), ' ')).append(" | ");
			}
			sb.append(lines.get(i)).append("\n");
		}
		return sb.toString();
	}

	private static String pad(String s, int n, char pad) {
		StringBuilder sb = new StringBuilder(n);
		sb.append(s);
		while (sb.length() < n) {
			sb.append(pad);
		}
		return sb.toString();
	}
}
