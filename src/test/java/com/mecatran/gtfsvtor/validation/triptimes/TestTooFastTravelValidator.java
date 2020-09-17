package com.mecatran.gtfsvtor.validation.triptimes;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.mecatran.gtfsvtor.model.GtfsRouteType;
import com.mecatran.gtfsvtor.test.stubs.TestConfig;

public class TestTooFastTravelValidator {

	@Test
	public void testDefMaxSpeed() {
		TooFastTravelValidator tftv = new TooFastTravelValidator();
		TestConfig config = new TestConfig();

		final int TAXI_CODE = 1500;

		assertEquals(100 / 3.6,
				tftv.getMaxSpeedMps(GtfsRouteType.BUS_CODE, config), 1e-3);
		assertEquals(100 / 3.6, tftv.getMaxSpeedMps(TAXI_CODE, config), 1e-3);
		config.put(config.getKey(tftv, "maxSpeedKph." + TAXI_CODE), "60");
		assertEquals(100 / 3.6,
				tftv.getMaxSpeedMps(GtfsRouteType.BUS_CODE, config), 1e-3);
		assertEquals(60 / 3.6, tftv.getMaxSpeedMps(TAXI_CODE, config), 1e-3);
		config = new TestConfig();
		config.put(config.getKey(tftv, "maxSpeedKph." + GtfsRouteType.BUS_CODE),
				"40");
		assertEquals(40 / 3.6,
				tftv.getMaxSpeedMps(GtfsRouteType.BUS_CODE, config), 1e-3);
		assertEquals(40 / 3.6, tftv.getMaxSpeedMps(TAXI_CODE, config), 1e-3);
		config.put(config.getKey(tftv, "maxSpeedKph." + TAXI_CODE), "50");
		assertEquals(40 / 3.6,
				tftv.getMaxSpeedMps(GtfsRouteType.BUS_CODE, config), 1e-3);
		assertEquals(50 / 3.6, tftv.getMaxSpeedMps(TAXI_CODE, config), 1e-3);
	}

}
