package com.mecatran.gtfsvtor.loader.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mecatran.gtfsvtor.loader.NamedInputStreamSource;

public class FileSystemDataSource implements NamedInputStreamSource {

	private File baseDir;
	private Set<String> readTables = new HashSet<>();

	public FileSystemDataSource(File baseDir) {
		this.baseDir = baseDir;
	}

	@Override
	public InputStream getInputStream(String tableName) throws IOException {
		readTables.add(tableName);
		File tableFile = new File(baseDir, tableName);
		return new FileInputStream(tableFile);
	}

	@Override
	public Collection<String> getUnreadEntries() {
		List<String> ret = new ArrayList<>();
		for (File file : baseDir.listFiles()) {
			/* Only report files as unknown file */
			if (!file.isFile())
				continue;
			String filename = file.getName();
			if (readTables.contains(file.getName()))
				continue;
			ret.add(filename);
		}
		Collections.sort(ret);
		return ret;
	}
}
