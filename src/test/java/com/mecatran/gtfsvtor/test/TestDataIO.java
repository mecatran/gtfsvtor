package com.mecatran.gtfsvtor.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

import com.mecatran.gtfsvtor.lib.GtfsVtorOptions.NamedDataIO;

public class TestDataIO implements NamedDataIO {

	private ByteArrayOutputStream out = new ByteArrayOutputStream();

	public TestDataIO() {
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return out;
	}

	@Override
	public Optional<InputStream> getInputStream() throws IOException {
		return Optional.empty();
	}

	@Override
	public String getName() {
		return "<Internal_byte_buffer>";
	}

	public byte[] getData() {
		return out.toByteArray();
	}
}
