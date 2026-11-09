package com.mecatran.gtfsvtor.validation;

import java.util.List;

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

		/** Value of a source column on the current row, or null when absent. */
		public default String getFieldValue(String name) {
			DataObjectSourceInfo source = getSourceInfo();
			if (source == null || source.getFields() == null)
				return null;
			List<String> fields = source.getFields();
			int index = source.getTable().getHeaderColumns().indexOf(name);
			return index >= 0 && index < fields.size() ? fields.get(index) : null;
		}

		public ReadOnlyDao getPartialDao();
	}

	public void validate(Class<? extends T> clazz, T object, Context context);
}
