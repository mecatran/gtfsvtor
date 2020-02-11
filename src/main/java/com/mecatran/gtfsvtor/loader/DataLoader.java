package com.mecatran.gtfsvtor.loader;

import com.mecatran.gtfsvtor.dao.AppendableDao;
import com.mecatran.gtfsvtor.dao.ReadOnlyDao;
import com.mecatran.gtfsvtor.model.GtfsObject;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.validation.StreamingValidator;

public interface DataLoader {

	public interface SourceContext {

		public ReportSink getReportSink();

		public DataObjectSourceInfo getSourceInfo();
	}

	public interface Context {

		public ReportSink getReportSink();

		public AppendableDao getDao();

		public ReadOnlyDao getReadOnlyDao();

		public StreamingValidator<GtfsObject<?>> getStreamingValidator();
	}

	public void load(Context context);
}
