package com.mecatran.gtfsvtor.cmdline;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

import com.mecatran.gtfsvtor.lib.GtfsVtorOptions.NamedDataIO;

public class FileDataIO implements NamedDataIO {

	private String filename;
	private boolean allowRead = false;

	public FileDataIO(String filename, boolean allowRead) {
		this.filename = filename;
		this.allowRead = allowRead;
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return new FileOutputStream(filename);
	}

	@Override
	public Optional<InputStream> getInputStream() throws IOException {
		if (!allowRead)
			return Optional.empty();
		File file = new File(filename);
		if (!file.canRead())
			return Optional.empty();
		return Optional.of(new FileInputStream(filename));
	}

	@Override
	public String getName() {
		return filename;
	}
}
