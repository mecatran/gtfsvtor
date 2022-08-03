package com.mecatran.gtfsvtor.utils;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.mecatran.gtfsvtor.reporting.FormattingOptions;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.impl.PlainTextIssueFormatter;

public class TestMiscUtils {

	@Test
	public void testTextJustify() {
		testOneTextJustify("", 10, "");
		testOneTextJustify("Simple", 6, "Simple");
		testOneTextJustify("Simple", 4, "Simple");
		testOneTextJustify("Simple", 10, "Simple");
		testOneTextJustify("Simple test", 4, "Simple", "test");
		testOneTextJustify("Simple test", 6, "Simple", "test");
		testOneTextJustify("Simple test", 7, "Simple", "test");
		testOneTextJustify("Simple test", 10, "Simple", "test");
		testOneTextJustify("Simple test", 11, "Simple test");
		testOneTextJustify("Simple test of text justification", 12,
				"Simple test", "of text", "justification");
		testOneTextJustify("Simple test of text justification", 32,
				"Simple test of text", "justification");
		testOneTextJustify("Simple test of text justification", 33,
				"Simple test of text justification");
		testOneTextJustify("Simple test of text\njustification", 60,
				"Simple test of text", "justification");
		testOneTextJustify("Simple test.\nOf text justification", 12,
				"Simple test.", "Of text", "justification");
		testOneTextJustify("Simple test.\n \n", 12, "Simple test.");
	}

	private void testOneTextJustify(String longText, int maxWidth,
			String... expectedLines) {
		List<String> lines = MiscUtils.wordProcessorSplit(longText, maxWidth);
		assertArrayEquals(expectedLines, lines.toArray(new String[0]));
	}

	@Test
	public void testDurationFormatting() {
		FormattingOptions opts = new FormattingOptions();
		IssueFormatter fmt = new PlainTextIssueFormatter(opts);
		assertEquals("42s", fmt.durationSec(42));
		assertEquals("-59s", fmt.durationSec(-59));
		assertEquals("2m00s", fmt.durationSec(120));
		assertEquals("-12m30s", fmt.durationSec(-750));
		assertEquals("2m00s", fmt.durationSec(120));
		assertEquals("2h23m00s", fmt.durationSec(8580));
	}
}
