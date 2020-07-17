package com.mecatran.gtfsvtor.loader;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

import com.mecatran.gtfsvtor.loader.impl.DataObjectSourceInfoImpl;
import com.mecatran.gtfsvtor.model.DataObjectSourceRef;

public interface DataTable extends Closeable, Iterable<DataRow> {

	@FunctionalInterface
	public interface Factory {
		public DataTable createDataTable(String tableName,
				InputStream inputStream) throws IOException;
	}

	@Override
	public Iterator<DataRow> iterator();

	public long getCurrentLineNumber();

	public String getTableName();

	public TableSourceInfo getTableSourceInfo();

	public default DataObjectSourceRef getSourceRef() {
		return new DataObjectSourceRef(getTableName(), 1L);
	}

	public default DataObjectSourceInfo getSourceInfo() {
		return new DataObjectSourceInfoImpl(getTableSourceInfo());
	}

	public List<String> getUnreadColumnHeaders();

	public List<String> getColumnHeaders();

	public List<String> getRawColumnHeaders();

	public Charset getCharset();

	public boolean isEmpty();
}
