package com.mecatran.gtfsvtor.validation;

import com.mecatran.gtfsvtor.dao.ReadOnlyDao;
import com.mecatran.gtfsvtor.loader.DataObjectSourceInfo;
import com.mecatran.gtfsvtor.model.DataObjectSourceRef;
import com.mecatran.gtfsvtor.model.GtfsObject;
import com.mecatran.gtfsvtor.reporting.ReportSink;

/**
 * A streaming validator validate an object without access to a DAO. This step
 * is usually done on the fly during data loading, before the object is added to
 * the DAO for indexing. All loaded objects will thus goes through this process,
 * even when missing primary ID or having duplicated IDs.
 */
public interface StreamingValidator<T extends GtfsObject<?>> {

	public interface Context {

		public ReportSink getReportSink();

		public DataObjectSourceRef getSourceRef();

		public DataObjectSourceInfo getSourceInfo();

		public ReadOnlyDao getPartialDao();
	}

	public void validate(Class<? extends T> clazz, T object, Context context);
}
