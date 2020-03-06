package com.mecatran.gtfsvtor.loader;

import java.io.Closeable;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

public interface DataTable extends Closeable, Iterable<DataRow> {

	@Override
	public Iterator<DataRow> iterator();

	public long getCurrentLineNumber();

	public TableSourceInfo getTableSourceInfo();

	public List<String> getUnreadColumnHeaders();

	public List<String> getColumnHeaders();

	public Charset getCharset();

	public boolean isEmpty();
}
