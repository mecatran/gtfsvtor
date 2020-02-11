package com.mecatran.gtfsvtor.loader.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.mecatran.gtfsvtor.loader.NamedInputStreamSource;

public class ZippedInputStreamSource implements NamedInputStreamSource {

	private Set<String> unreadEntries = new HashSet<>();
	private ZipFile zipFile;

	public ZippedInputStreamSource(File file) throws IOException {
		zipFile = new ZipFile(file);
		for (Enumeration<? extends ZipEntry> i = zipFile.entries(); i
				.hasMoreElements();) {
			ZipEntry entry = i.nextElement();
			unreadEntries.add(entry.getName());
		}
	}

	@Override
	public InputStream getInputStream(String tableName) throws IOException {
		unreadEntries.remove(tableName);
		ZipEntry entry = zipFile.getEntry(tableName);
		if (entry == null)
			throw new FileNotFoundException(
					zipFile.getName() + "/" + tableName);
		return zipFile.getInputStream(entry);
	}

	@Override
	public Collection<String> getUnreadEntries() {
		List<String> ret = new ArrayList<>(unreadEntries);
		Collections.sort(ret);
		return ret;
	}
}
