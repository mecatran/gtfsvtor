package com.mecatran.gtfsvtor.loader.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.mecatran.gtfsvtor.loader.DataRow;
import com.mecatran.gtfsvtor.loader.DataTable;
import com.mecatran.gtfsvtor.reporting.impl.PlainTextIssueFormatter;

public class TestLineEndingReader {

	@Test
	public void countsAcrossReadsWithoutChangingCharacters() throws Exception {
		String text = "header\nquoted\r\nthird\nfourth\nfifth\nunterminated";
		try (LineEndingReader reader = new LineEndingReader(new StringReader(text))) {
			char[] single = new char[3];
			StringBuilder copy = new StringBuilder();
			while (reader.read(single, 1, 1) != -1)
				copy.append(single[1]);
			assertEquals(text, copy.toString());
			PlainTextIssueFormatter formatter = new PlainTextIssueFormatter(null);
			reader.getMixedLineEndings("test.txt").get().format(formatter);
			assertEquals("test.txt mixes LF and CRLF line endings. "
					+ "LF count: 4 (first lines: [1, 3, 4]). CRLF count: 1 (first lines: [2]).",
					formatter.getPlainTextResult());
		}
	}

	@Test
	public void bothParsersReportMixedEndingsAfterDecodingBom() throws Exception {
		for (DataTable.Factory factory : List.of(UnivocityCsvDataTable.factory(),
				ApacheCommonsCsvDataTable.factory())) {
			byte[] csv = "\uFEFFname\none\r\ntwo\n".getBytes(StandardCharsets.UTF_16LE);
			try (DataTable table = factory.createDataTable("test.txt", new ByteArrayInputStream(csv))) {
				List<String> rows = new ArrayList<>();
				for (DataRow row : table)
					rows.add(row.getString("name"));
				assertEquals(List.of("one", "two"), rows);
				PlainTextIssueFormatter formatter = new PlainTextIssueFormatter(null);
				table.getMixedLineEndings().get().format(formatter);
				assertEquals("test.txt mixes LF and CRLF line endings. "
						+ "LF count: 2 (first lines: [1, 3]). CRLF count: 1 (first lines: [2]).",
						formatter.getPlainTextResult());
			}
		}
	}

	@Test
	public void bothParsersPreserveRowsAndAcceptEitherUniformStyle() throws Exception {
		for (DataTable.Factory factory : List.of(UnivocityCsvDataTable.factory(),
				ApacheCommonsCsvDataTable.factory())) {
			for (String eol : List.of("\n", "\r\n")) {
				String text = "name" + eol + "one" + eol + "two";
				try (DataTable table = factory.createDataTable("test.txt",
						new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)))) {
					List<String> rows = new ArrayList<>();
					for (DataRow row : table)
						rows.add(row.getString("name"));
					assertEquals(List.of("one", "two"), rows);
					assertFalse(table.getMixedLineEndings().isPresent());
				}
			}
		}
	}
}
