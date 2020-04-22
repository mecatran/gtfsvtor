package com.mecatran.gtfsvtor.loader;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

public interface DataTable extends Closeable, Iterable<DataRow> {

	@FunctionalInterface
	public interface Factory {
		public DataTable createDataTable(String tableName,
				InputStream inputStream) throws IOException;
	}

	@Override
	public Iterator<DataRow> iterator();

	public long getCurrentLineNumber();

	public TableSourceInfo getTableSourceInfo();

	public List<String> getUnreadColumnHeaders();

	public List<String> getColumnHeaders();

	public Charset getCharset();

	public boolean isEmpty();
}
