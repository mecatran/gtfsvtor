package com.mecatran.gtfsvtor.reporting;

import java.io.IOException;

public interface ReportFormatter {

	public void format(ReviewReport report) throws IOException;
}
