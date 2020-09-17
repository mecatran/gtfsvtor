package com.mecatran.gtfsvtor.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

import com.mecatran.gtfsvtor.lib.GtfsVtorOptions.NamedDataIO;

// TODO Move to stubs package
public class TestDataIO implements NamedDataIO {

	private ByteArrayOutputStream out = new ByteArrayOutputStream();
	private byte[] inData;

	public TestDataIO() {
	}

	public TestDataIO(byte[] data) {
		this.inData = data;
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return out;
	}

	@Override
	public Optional<InputStream> getInputStream() throws IOException {
		if (inData != null) {
			return Optional.of(new ByteArrayInputStream(inData));
		} else {
			return Optional.empty();
		}
	}

	@Override
	public String getName() {
		return "<Internal_byte_buffer>";
	}

	public byte[] getData() {
		return out.toByteArray();
	}
}
