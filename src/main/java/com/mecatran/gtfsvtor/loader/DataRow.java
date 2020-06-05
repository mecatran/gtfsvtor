package com.mecatran.gtfsvtor.loader;

import com.mecatran.gtfsvtor.model.DataObjectSourceRef;

public interface DataRow {

	/**
	 * @param field Field name to return.
	 * @return null if empty or undefined, trimmed field value otherwise.
	 */
	public String getString(String field);

	public DataObjectSourceInfo getSourceInfo();

	public DataObjectSourceRef getSourceRef();

	public int getRecordCount();
}
