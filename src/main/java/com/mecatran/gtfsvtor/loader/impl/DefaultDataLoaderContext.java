package com.mecatran.gtfsvtor.loader.impl;

import com.mecatran.gtfsvtor.dao.AppendableDao;
import com.mecatran.gtfsvtor.dao.ReadOnlyDao;
import com.mecatran.gtfsvtor.loader.DataLoader;
import com.mecatran.gtfsvtor.model.GtfsObject;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.validation.StreamingValidator;

public class DefaultDataLoaderContext implements DataLoader.Context {

	private AppendableDao appendableDao;
	private ReadOnlyDao readOnlyDao;
	private ReportSink reportSink;
	private StreamingValidator<GtfsObject<?>> streamingValidator;

	public DefaultDataLoaderContext(AppendableDao appendableDao,
			ReadOnlyDao readOnlyDao, ReportSink reportSink,
			StreamingValidator<GtfsObject<?>> streamingValidator) {
		this.appendableDao = appendableDao;
		this.readOnlyDao = readOnlyDao;
		this.reportSink = reportSink;
		this.streamingValidator = streamingValidator;
	}

	@Override
	public ReportSink getReportSink() {
		return reportSink;
	}

	@Override
	public AppendableDao getAppendableDao() {
		return appendableDao;
	}

	@Override
	public ReadOnlyDao getReadOnlyDao() {
		return readOnlyDao;
	}

	@Override
	public StreamingValidator<GtfsObject<?>> getStreamingValidator() {
		return streamingValidator;
	}
}
