package com.mecatran.gtfsvtor.validation;

public interface ValidatorConfig {

	public String getString(String key);

	public default String getString(String key, String defaultValue) {
		String str = this.getString(key);
		if (str == null)
			return defaultValue;
		return str;
	}

	public default Boolean getBoolean(String key, Boolean defaultValue) {
		String str = this.getString(key);
		if (str == null || str.isEmpty())
			return defaultValue;
		/*
		 * Being lenient on accepted values is a long-term pain, but in this
		 * case accept common values.
		 */
		if (str.equals("1") || str.equals("true"))
			return true;
		if (str.equals("0") || str.equals("false"))
			return false;
		return defaultValue;
	}

	public default Long getLong(String key, Long defaultValue) {
		String str = this.getString(key);
		if (str == null || str.isEmpty())
			return defaultValue;
		try {
			return Long.parseLong(str);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public default Double getDouble(String key, Double defaultValue) {
		String str = this.getString(key);
		if (str == null || str.isEmpty())
			return defaultValue;
		try {
			return Double.parseDouble(str);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public default String getKey(Object validator) {
		return getKey(validator, null);
	}

	public default String getKey(Object validator, String keySuffix) {
		// TODO Make sure this is sane
		// TODO Does this method belongs here?
		return "validator." + validator.getClass().getSimpleName()
				+ (keySuffix == null ? "" : ("." + keySuffix));
	}
}
