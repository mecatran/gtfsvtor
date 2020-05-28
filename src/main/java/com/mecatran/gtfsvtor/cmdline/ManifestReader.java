package com.mecatran.gtfsvtor.cmdline;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class ManifestReader {

	public static String IMPL_VERSION = "Implementation-Version";
	public static String BUILD_REV = "Build-Revision";
	public static String BUILD_DATE = "Build-Date";

	private Attributes attr;

	public ManifestReader(Class<?> clazz) {
		String className = clazz.getSimpleName() + ".class";
		String classPath = clazz.getResource(className).toString();
		if (!classPath.startsWith("jar")) {
			return;
		}
		String manifestPath = classPath.substring(0,
				classPath.lastIndexOf("!") + 1) + "/META-INF/MANIFEST.MF";
		try {
			URL url = new URL(manifestPath);
			InputStream input = url.openStream();
			Manifest manifest = new Manifest(input);
			input.close();
			attr = manifest.getMainAttributes();
		} catch (IOException e) {
			System.err.println("Cannot read manifest: " + e);
		}
	}

	public String getValue(String attrName, String def) {
		if (attr == null)
			return def;
		String value = attr.getValue(attrName);
		if (value == null)
			return def;
		return value;
	}

	public String getApplicationVersion() {
		return getValue(IMPL_VERSION, "dev");
	}

	public String getApplicationBuildRevision() {
		return getValue(BUILD_REV, "dev");
	}

	public String getApplicationBuildDate() {
		return getValue(BUILD_DATE, "now");
	}
}
