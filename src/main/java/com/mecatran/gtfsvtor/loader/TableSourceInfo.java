package com.mecatran.gtfsvtor.loader;

import java.util.List;

public interface TableSourceInfo extends Comparable<TableSourceInfo> {

	public String getTableName();

	public List<String> getHeaderColumns();
}
