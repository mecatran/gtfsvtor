package com.mecatran.gtfsvtor.loader;

import java.io.IOException;
import java.util.Collection;

public interface NamedTabularDataSource {

	public DataTable getDataTable(String tableName) throws IOException;

	public Collection<String> getUnreadEntries();
}
