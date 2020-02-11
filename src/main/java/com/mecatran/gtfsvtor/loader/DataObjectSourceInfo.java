package com.mecatran.gtfsvtor.loader;

import java.util.List;

public interface DataObjectSourceInfo extends Comparable<DataObjectSourceInfo> {

	public TableSourceInfo getTable();

	public List<String> getFields();

	public long getLineNumber();
}
