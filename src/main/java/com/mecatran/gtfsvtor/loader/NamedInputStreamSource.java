package com.mecatran.gtfsvtor.loader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import com.mecatran.gtfsvtor.loader.impl.FileSystemDataSource;
import com.mecatran.gtfsvtor.loader.impl.ZippedInputStreamSource;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.GeneralIOError;

public interface NamedInputStreamSource {

	public InputStream getInputStream(String tableName) throws IOException;

	public Collection<String> getUnreadEntries();

	public static NamedInputStreamSource autoGuess(String path,
			ReportSink reportSink) {
		File file = new File(path);
		if (!file.exists()) {
			reportSink.report(
					new GeneralIOError("File " + path + " does not exists."));
			return null;
		}
		if (file.isDirectory()) {
			return new FileSystemDataSource(file);
		} else {
			try {
				return new ZippedInputStreamSource(file);
			} catch (IOException e) {
				reportSink.report(new GeneralIOError(
						e.getLocalizedMessage() + " (" + path + ")"));
				return null;
			}
		}
	}
}
