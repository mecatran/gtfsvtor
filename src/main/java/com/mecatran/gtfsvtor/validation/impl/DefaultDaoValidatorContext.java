package com.mecatran.gtfsvtor.validation.impl;

import com.mecatran.gtfsvtor.dao.IndexedReadOnlyDao;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.validation.DaoValidator;
import com.mecatran.gtfsvtor.validation.ValidatorConfig;

public class DefaultDaoValidatorContext implements DaoValidator.Context {

	private IndexedReadOnlyDao dao;
	private ReportSink reportSink;
	private ValidatorConfig config;

	public DefaultDaoValidatorContext(IndexedReadOnlyDao dao,
			ReportSink reportSink, ValidatorConfig config) {
		this.dao = dao;
		this.reportSink = reportSink;
		this.config = config;
	}

	@Override
	public IndexedReadOnlyDao getDao() {
		return dao;
	}

	@Override
	public ReportSink getReportSink() {
		return reportSink;
	}

	@Override
	public ValidatorConfig getConfig() {
		return config;
	}
}
