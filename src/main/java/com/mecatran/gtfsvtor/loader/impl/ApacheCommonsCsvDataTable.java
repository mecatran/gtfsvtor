package com.mecatran.gtfsvtor.loader.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;

import com.mecatran.gtfsvtor.loader.DataRow;
import com.mecatran.gtfsvtor.loader.DataTable;
import com.mecatran.gtfsvtor.loader.TableSourceInfo;

public class ApacheCommonsCsvDataTable implements DataTable {

	private String tableName;
	private CSVParser csvParser;
	private Iterator<CSVRecord> csvIterator;
	private Set<String> readFields = new HashSet<>();
	private Charset charset;

	private boolean checkRecordConsistent = false;

	public static DataTable.Factory factory() {
		return (tableName, inputStream) -> new ApacheCommonsCsvDataTable(
				tableName, inputStream);
	}

	public ApacheCommonsCsvDataTable(String tableName, InputStream inputStream)
			throws IOException {
		this.tableName = tableName;

		charset = StandardCharsets.UTF_8;

		BOMInputStream bomIn = new BOMInputStream(inputStream,
				ByteOrderMark.UTF_8, ByteOrderMark.UTF_16LE,
				ByteOrderMark.UTF_16BE, ByteOrderMark.UTF_32LE,
				ByteOrderMark.UTF_32BE);

		if (bomIn.hasBOM() == false) {
			// No BOM found, OK
		} else if (bomIn.hasBOM(ByteOrderMark.UTF_8)) {
			// UTF8 BOK, OK
		} else if (bomIn.hasBOM(ByteOrderMark.UTF_16LE)) {
			charset = StandardCharsets.UTF_16LE;
		} else if (bomIn.hasBOM(ByteOrderMark.UTF_16BE)) {
			charset = StandardCharsets.UTF_16BE;
		} else if (bomIn.hasBOM(ByteOrderMark.UTF_32LE)) {
			charset = Charset.forName("UTF-32LE");
			// throw new IOException("UTF-32 little-endian BOM encoding, not
			// supported");
		} else if (bomIn.hasBOM(ByteOrderMark.UTF_32BE)) {
			charset = Charset.forName("UTF-32BE");
			// throw new IOException("UTF-32 big-endian BOM encoding, , not
			// supported");
		}

		CharsetDecoder decoder = charset.newDecoder().replaceWith("\uFFFD")
				.onMalformedInput(CodingErrorAction.REPLACE);
		final BufferedReader br = new BufferedReader(
				new InputStreamReader(bomIn, decoder));
		CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',').withHeader()
				.withNullString(null).withIgnoreSurroundingSpaces(true);
		csvParser = new CSVParser(br, format);
		csvIterator = csvParser.iterator();
	}

	@Override
	public Iterator<DataRow> iterator() {
		return new Iterator<DataRow>() {

			@Override
			public boolean hasNext() {
				return csvIterator.hasNext();
			}

			@Override
			public DataRow next() {
				CSVRecord record = csvIterator.next();
				if (checkRecordConsistent && !record.isConsistent()) {
					throw new IllegalArgumentException(String.format(
							"Invalid line column count L%d (%s): %d vs %d columns in header.",
							csvParser.getCurrentLineNumber(),
							Arrays.toString(record.toMap().values().toArray()),
							record.toMap().size(),
							csvParser.getHeaderMap().size()));
				}
				return new ApacheCommonsCsvDataRow(
						ApacheCommonsCsvDataTable.this, record);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("remove()");
			}
		};
	}

	void recordReadField(String fieldName) {
		readFields.add(fieldName);
	}

	@Override
	public long getCurrentLineNumber() {
		return csvParser.getCurrentLineNumber();
	}

	@Override
	public String getTableName() {
		return tableName;
	}

	@Override
	public void close() throws IOException {
		csvParser.close();
	}

	@Override
	public TableSourceInfo getTableSourceInfo() {
		return new TableSourceInfoImpl(tableName, getColumnHeaders());
	}

	@Override
	public List<String> getUnreadColumnHeaders() {
		List<String> ret = new ArrayList<>(csvParser.getHeaderNames());
		for (String readField : readFields) {
			// This is O(n), but number of CSV headers is probably low
			while (ret.remove(readField)) {
			}
		}
		return ret;
	}

	@Override
	public List<String> getColumnHeaders() {
		return Collections.unmodifiableList(csvParser.getHeaderNames());
	}

	@Override
	public Charset getCharset() {
		return charset;
	}

	@Override
	public boolean isEmpty() {
		return csvParser.getHeaderNames().isEmpty()
				&& csvParser.getCurrentLineNumber() == 0;
	}
}
