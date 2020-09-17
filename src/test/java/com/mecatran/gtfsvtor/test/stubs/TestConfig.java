package com.mecatran.gtfsvtor.test.stubs;

import java.util.HashMap;
import java.util.Map;

import com.mecatran.gtfsvtor.validation.ValidatorConfig;

public class TestConfig implements ValidatorConfig {

	private Map<String, String> values = new HashMap<>();

	public TestConfig() {
	}

	public TestConfig put(String key, String value) {
		this.values.put(key, value);
		return this;
	}

	@Override
	public String getString(String key) {
		return values.get(key);
	}

}
