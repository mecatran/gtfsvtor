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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;

import com.mecatran.gtfsvtor.loader.DataRow;
import com.mecatran.gtfsvtor.loader.DataTable;
import com.mecatran.gtfsvtor.loader.TableSourceInfo;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

public class UnivocityCsvDataTable implements DataTable {

	private String tableName;
	private CsvParser csvParser;
	private Set<String> readFields = new HashSet<>();
	private Charset charset;
	private Map<String, Integer> headerIndex = new HashMap<>();
	private int headerSize = 0;
	// TODO Enable this option
	private boolean checkRecordConsistent = false;
	private boolean emptyFile = false;

	public static DataTable.Factory factory() {
		return (tableName, inputStream) -> new UnivocityCsvDataTable(tableName,
				inputStream);
	}

	public UnivocityCsvDataTable(String tableName, InputStream inputStream)
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

		CsvParserSettings settings = new CsvParserSettings();
		settings.getFormat().setLineSeparator("\n");
		settings.getFormat().setDelimiter(',');
		settings.setHeaderExtractionEnabled(true);
		settings.setNullValue(null);
		settings.setIgnoreLeadingWhitespaces(true);
		settings.setIgnoreTrailingWhitespaces(true);
		csvParser = new CsvParser(settings);

		csvParser.beginParsing(br);

		String[] headers = csvParser.getContext().headers();
		if (headers == null) {
			emptyFile = true;
		} else {
			emptyFile = false;
			headerSize = headers.length;
			for (int i = 0; i < headers.length; i++) {
				headerIndex.put(headers[i], i);
			}
		}
	}

	@Override
	public Iterator<DataRow> iterator() {
		return new Iterator<DataRow>() {

			private String[] record;

			@Override
			public boolean hasNext() {
				record = csvParser.parseNext();
				return record != null;
			}

			@Override
			public DataRow next() {
				if (record == null)
					throw new NoSuchElementException();
				if (checkRecordConsistent && record.length != headerSize) {
					throw new IllegalArgumentException(String.format(
							"Invalid line column count L%d (%s): %d vs %d columns in header.",
							getCurrentLineNumber(), Arrays.toString(record),
							record.length, headerSize));
				}
				return new UnivocityCsvDataRow(UnivocityCsvDataTable.this,
						record);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("remove()");
			}
		};
	}

	int fieldIndex(String fieldName) {
		if (getCurrentLineNumber() <= 2)
			readFields.add(fieldName);
		return headerIndex.getOrDefault(fieldName, -1);
	}

	@Override
	public long getCurrentLineNumber() {
		return csvParser.getContext().currentLine();
	}

	@Override
	public void close() throws IOException {
		// No-op, auto-close enabled
	}

	@Override
	public TableSourceInfo getTableSourceInfo() {
		return new TableSourceInfoImpl(tableName, getColumnHeaders());
	}

	@Override
	public List<String> getUnreadColumnHeaders() {
		List<String> ret = new ArrayList<>(getColumnHeaders());
		for (String readField : readFields) {
			// This is O(n), but number of CSV headers is probably low
			while (ret.remove(readField)) {
			}
		}
		return ret;
	}

	@Override
	public List<String> getColumnHeaders() {
		List<String> ret = new ArrayList<>();
		String[] headers = csvParser.getContext().headers();
		if (headers != null) {
			for (String header : headers) {
				ret.add(header);
			}
		}
		return ret;
	}

	@Override
	public Charset getCharset() {
		return charset;
	}

	@Override
	public boolean isEmpty() {
		return emptyFile;
	}
}
