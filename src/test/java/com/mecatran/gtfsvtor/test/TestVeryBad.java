package com.mecatran.gtfsvtor.test;

import static com.mecatran.gtfsvtor.test.TestUtils.loadAndValidate;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.mecatran.gtfsvtor.loader.DataObjectSourceInfo;
import com.mecatran.gtfsvtor.model.GtfsAgency;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.issues.DuplicatedColumnError;
import com.mecatran.gtfsvtor.test.TestUtils.TestBundle;

public class TestVeryBad {

	@Test
	public void testVerybad() {
		TestBundle tb = loadAndValidate("verybad");

		// This *will* break, check and adjust when necessary
		assertEquals(6, tb.issuesCountOfSeverities(ReportIssueSeverity.INFO));
		assertEquals(107,
				tb.issuesCountOfSeverities(ReportIssueSeverity.WARNING));
		assertEquals(125,
				tb.issuesCountOfSeverities(ReportIssueSeverity.ERROR));
		assertEquals(0,
				tb.issuesCountOfSeverities(ReportIssueSeverity.CRITICAL));

		// Check if source info are properly loaded
		DuplicatedColumnError dce = tb.report
				.getReportIssues(DuplicatedColumnError.class).findFirst().get();
		assertEquals(1, dce.getSourceRefs().size());
		DataObjectSourceInfo si = tb.report
				.getSourceInfo(dce.getSourceRefs().get(0).getSourceRef());
		assertEquals(GtfsAgency.TABLE_NAME, si.getTable().getTableName());
		assertEquals(1, si.getLineNumber());
	}
}
