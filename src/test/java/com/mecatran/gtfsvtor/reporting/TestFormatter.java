package com.mecatran.gtfsvtor.reporting;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.mecatran.gtfsvtor.reporting.FormattingOptions.SpeedUnit;
import com.mecatran.gtfsvtor.reporting.html.HtmlIssueFormatter;
import com.mecatran.gtfsvtor.reporting.impl.PlainTextIssueFormatter;

public class TestFormatter {

	@Test
	public void testHtmlSpeedUnit() throws IOException {
		HtmlIssueFormatter fmt1 = new HtmlIssueFormatter(
				new FormattingOptions(SpeedUnit.KPH));
		assertEquals("3.60 km/h", fmt1.speed(1.0));
		HtmlIssueFormatter fmt2 = new HtmlIssueFormatter(
				new FormattingOptions(SpeedUnit.MPS));
		assertEquals("1.00 m/s", fmt2.speed(1.0));
		HtmlIssueFormatter fmt3 = new HtmlIssueFormatter(
				new FormattingOptions(SpeedUnit.MPH));
		assertEquals("2.24 mph", fmt3.speed(1.0));
	}

	@Test
	public void testTextSpeedUnit() throws IOException {
		PlainTextIssueFormatter fmt1 = new PlainTextIssueFormatter(
				new FormattingOptions(SpeedUnit.KPH));
		assertEquals("3.60 km/h", fmt1.speed(1.0));
		PlainTextIssueFormatter fmt2 = new PlainTextIssueFormatter(
				new FormattingOptions(SpeedUnit.MPS));
		assertEquals("1.00 m/s", fmt2.speed(1.0));
		PlainTextIssueFormatter fmt3 = new PlainTextIssueFormatter(
				new FormattingOptions(SpeedUnit.MPH));
		assertEquals("2.24 mph", fmt3.speed(1.0));
	}
}
