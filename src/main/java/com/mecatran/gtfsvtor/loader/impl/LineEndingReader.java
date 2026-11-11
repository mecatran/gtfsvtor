package com.mecatran.gtfsvtor.loader.impl;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mecatran.gtfsvtor.reporting.issues.MixedLineEndingsError;

/** Observes physical line endings without changing the CSV parser's input. */
final class LineEndingReader extends Reader {

	private static final int SAMPLE_LIMIT = 3;
	private final Reader input;
	private final List<Long> lfLines = new ArrayList<>();
	private final List<Long> crlfLines = new ArrayList<>();
	private long lfCount;
	private long crlfCount;
	private boolean previousWasCR;

	LineEndingReader(Reader input) {
		this.input = input;
	}

	@Override
	public int read(char[] buffer, int offset, int length) throws IOException {
		int count = input.read(buffer, offset, length);
		for (int i = offset; i < offset + count; i++) {
			char c = buffer[i];
			if (c == '\n') {
				long line = lfCount + crlfCount + 1;
				List<Long> sample = previousWasCR ? crlfLines : lfLines;
				if (sample.size() < SAMPLE_LIMIT)
					sample.add(line);
				if (previousWasCR)
					crlfCount++;
				else
					lfCount++;
			}
			previousWasCR = c == '\r';
		}
		return count;
	}

	Optional<MixedLineEndingsError> getMixedLineEndings(String tableName) {
		if (lfCount == 0 || crlfCount == 0)
			return Optional.empty();
		return Optional.of(new MixedLineEndingsError(tableName, lfCount, lfLines,
				crlfCount, crlfLines));
	}

	@Override
	public void close() throws IOException {
		input.close();
	}
}
