package com.mecatran.gtfsvtor.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.text.ParseException;

import org.junit.Test;

public class TestLogicalTime {

	@Test
	public void testParse() throws ParseException {
		GtfsLogicalTime time1 = GtfsLogicalTime.parseFromHH_MM_SS("10:30:50");
		assertEquals(GtfsLogicalTime.getTime(10, 30, 50), time1);
		assertEquals(10 * 3600 + 30 * 60 + 50, time1.getSecondSinceMidnight());
		assertEquals(10, time1.getHour());
		assertEquals(30, time1.getMinute());
		assertEquals(50, time1.getSecond());

		assertNull(GtfsLogicalTime.parseFromHH_MM_SS(null));
		assertNull(GtfsLogicalTime.parseFromHH_MM_SS(""));

		assertException(() -> GtfsLogicalTime.parseFromHH_MM_SS("10:30"));
		assertException(() -> GtfsLogicalTime.parseFromHH_MM_SS("x"));
		assertException(() -> GtfsLogicalTime.parseFromHH_MM_SS("999:30:00"));
	}

	@FunctionalInterface
	private interface MethodThatRaiseParseException {
		void call() throws ParseException;
	}

	private void assertException(MethodThatRaiseParseException method) {
		try {
			method.call();
		} catch (ParseException e) {
			return;
		}
		fail("Should have raised an exception");
	}

	@Test
	public void testEquals() {
		GtfsLogicalTime time1 = GtfsLogicalTime.getTime(10, 30, 0);
		GtfsLogicalTime time2 = GtfsLogicalTime.getTime(10, 30, 0);
		assertEquals(time1, time2);
		GtfsLogicalTime time3 = GtfsLogicalTime.getTime(10, 30, 1);
		assertNotEquals(time1, time3);
	}
}
