package com.mecatran.gtfsvtor.validation;

import com.mecatran.gtfsvtor.dao.IndexedReadOnlyDao;
import com.mecatran.gtfsvtor.reporting.ReportSink;

public interface DaoValidator {

	public interface Context {

		public IndexedReadOnlyDao getDao();

		public ReportSink getReportSink();

		public ValidatorConfig getConfig();
	}

	public void validate(Context context);
}
