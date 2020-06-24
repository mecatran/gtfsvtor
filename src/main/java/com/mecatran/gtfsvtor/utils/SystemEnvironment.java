package com.mecatran.gtfsvtor.utils;

import java.util.Date;

/**
 * All this to provide a consistent and replicable environment for test
 * purposes.
 * 
 * There is maybe a cleaner way to achieve this, but for now this will be ok.
 */
public class SystemEnvironment {

	private static Date fakedNow = null;

	public static Date now() {
		return fakedNow == null ? new Date() : fakedNow;
	}

	public static void setFakedNow(Date faked) {
		fakedNow = faked;
	}
}
