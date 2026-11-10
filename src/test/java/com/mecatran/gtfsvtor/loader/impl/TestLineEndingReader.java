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
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.issues.MixedLineEndingsError;

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
			MixedLineEndingsError issue = reader.getMixedLineEndings("test.txt").get();
			assertEquals(ReportIssueSeverity.ERROR, issue.getSeverity());
			assertEquals("test.txt", issue.getTableName());
			assertEquals(4L, issue.getLfCount());
			assertEquals(List.of(1L, 3L, 4L), issue.getLfLines());
			assertEquals(1L, issue.getCrlfCount());
			assertEquals(List.of(2L), issue.getCrlfLines());
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
				MixedLineEndingsError issue = table.getMixedLineEndings().get();
				assertEquals(ReportIssueSeverity.ERROR, issue.getSeverity());
				assertEquals("test.txt", issue.getTableName());
				assertEquals(2L, issue.getLfCount());
				assertEquals(List.of(1L, 3L), issue.getLfLines());
				assertEquals(1L, issue.getCrlfCount());
				assertEquals(List.of(2L), issue.getCrlfLines());
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
