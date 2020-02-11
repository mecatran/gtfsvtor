package com.mecatran.gtfsvtor.validation.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.mecatran.gtfsvtor.validation.ValidatorConfig;

public class DefaultValidatorConfig implements ValidatorConfig {

	private Map<String, String> properties = new HashMap<>();

	public DefaultValidatorConfig() {
	}

	public void loadProperties(File propFile) {
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(propFile));
		} catch (IOException e) {
			// TODO proper log
			System.err.println("Cannot load " + propFile);
		}
		for (Map.Entry<Object, Object> kv : props.entrySet()) {
			properties.put(kv.getKey().toString(), kv.getValue().toString());
		}
	}

	@Override
	public String getString(String key) {
		return properties.get(key);
	}

}
