package com.mecatran.gtfsvtor.model.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import com.mecatran.gtfsvtor.dao.impl.PackingStopTimesDao;
import com.mecatran.gtfsvtor.model.GtfsDropoffType;
import com.mecatran.gtfsvtor.model.GtfsLogicalTime;
import com.mecatran.gtfsvtor.model.GtfsPickupType;
import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.model.GtfsStopTime;
import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.model.GtfsTripStopSequence;

public class TestPackedStopTimes {

	@Test
	public void testSample() throws ParseException {
		PackingStopTimesDao.DefaultContext ctx = new PackingStopTimesDao.DefaultContext();
		List<GtfsStopTime> stopTimes = new ArrayList<>();

		// Simple basic test
		stopTimes.add(
				stopTime("T1", 0, "S1", 0, "8:00:00", 0, "8:00:00", 0.0, null));
		testList(ctx, stopTimes);

		// Same values, deltas = 0
		stopTimes.add(
				stopTime("T1", 0, "S1", 0, "8:00:00", 0, "8:00:00", 0.0, null));
		// Simple deltas (1, 1 minute)
		stopTimes.add(stopTime("T1", 1, "S2", 2, "8:01:00", 2, "8:02:00", 100.0,
				null));
		// Small minute deltas (2, 2 minutes)
		stopTimes.add(stopTime("T1", 3, "S3", 1, "8:03:00", 1, "8:05:00", 200.0,
				null));
		// Minute deltas (10 minutes)
		stopTimes.add(stopTime("T1", 13, "S3", 3, "8:13:00", 3, "8:23:00",
				210.0, null));
		// Minute deltas (250 minutes)
		stopTimes.add(stopTime("T1", 14, "S4", 1, "12:33:00", 3, "16:43:00",
				212.0, null));
		// Small second deltas (12 seconds)
		stopTimes.add(stopTime("T1", 15, "S2", 2, "16:43:12", 2, "16:43:24",
				222.0, null));
		// Medium second deltas (610 seconds)
		stopTimes.add(stopTime("T1", 15, "S2", 2, "16:53:34", 2, "17:03:44",
				244.567, null));
		// Large second deltas (86410 seconds)
		stopTimes.add(stopTime("T1", 15, "S2", 2, "41:03:54", 2, "65:04:04",
				6199.111, null));
		// Negative deltas
		stopTimes.add(stopTime("T1", 100, "S1", 0, "8:00:30", 0, "8:00:00",
				10.0, null));
		testList(ctx, stopTimes);

		// Null values
		stopTimes.add(stopTime("T1", 100, null, null, (String) null, null,
				(String) null, null, null));
		testList(ctx, stopTimes);

		// Large delta, with headsign
		stopTimes.add(stopTime("T1", 123456789, "S4", 1, "28:56:12", 1,
				"37:45:23", 10000000.0, "Headsign 1"));
		testList(ctx, stopTimes);

		// Same headsign as before, large negative delta
		stopTimes.add(stopTime("T1", 123456790, "S5", 0, "4:00:00", 1,
				"4:00:00", 123.45, "Headsign 1"));
		testList(ctx, stopTimes);
	}

	@Test
	public void testTimesDeltas() throws ParseException {
		PackingStopTimesDao.DefaultContext ctx = new PackingStopTimesDao.DefaultContext();
		for (int base = 0; base < 24 * 60 * 60; base += 23597) {
			for (int delta = -24 * 60 * 60; delta < 72 * 60 * 60; delta++) {
				List<GtfsStopTime> stopTimes = new ArrayList<>();
				stopTimes.add(
						stopTime("T1", 1, "S1", 0, base, 0, base, null, null));
				stopTimes.add(stopTime("T1", 2, "S2", 0, base + delta, 0,
						base + delta, null, null));
				testList(ctx, stopTimes);
				stopTimes = new ArrayList<>();
				stopTimes.add(
						stopTime("T1", 1, "S1", 0, base, 0, base, null, null));
				stopTimes.add(stopTime("T1", 2, "S2", 0, base, 0, base + delta,
						null, null));
				testList(ctx, stopTimes);
			}
		}
	}

	@Test
	public void testHeadsigns() throws ParseException {
		PackingStopTimesDao.DefaultContext ctx = new PackingStopTimesDao.DefaultContext();
		Random rand = new Random(42L);
		List<GtfsStopTime> stopTimes = new ArrayList<>();
		for (int i = 0; i < 10000; i++) {
			stopTimes.add(stopTime("T1", i, "S" + i, 0, "8:00:00", 0, "8:00:30",
					i * 10.0, "Headsign #" + rand.nextInt(1000)));
		}
		testList(ctx, stopTimes);
	}

	@Test
	public void testRandom() throws ParseException {
		PackingStopTimesDao.DefaultContext ctx = new PackingStopTimesDao.DefaultContext();
		Random rand = new Random(42L);
		for (int i = 0; i < 10000; i++) {
			List<GtfsStopTime> stopTimes = new ArrayList<>();
			double sd = rand.nextFloat() * 1000.0;
			int time = rand.nextInt(10000);
			int seq = rand.nextBoolean() ? 0 : rand.nextInt(100);
			int n = rand.nextInt(100) + 1;
			for (int j = 0; j < n; j++) {
				int slack = rand.nextBoolean() ? 0 : rand.nextInt(600) - 60;
				String stopId = "S" + rand.nextInt(n);
				String headsign = null;
				if (rand.nextInt(10) < 5)
					headsign = "Headsign #" + rand.nextInt(n);
				stopTimes.add(stopTime("T1", seq, stopId, rand.nextInt(4), time,
						rand.nextInt(4), time + slack, sd, headsign));
				switch (rand.nextInt(4)) {
				case 0:
					break;
				case 1:
					time = ((time / 60) + 1) * 60;
					time += 60 + rand.nextInt(4);
					break;
				case 2:
					time += rand.nextInt(300) - 30;
					break;
				case 3:
					time += rand.nextInt(3 * 3600) - 600;
					break;
				}
				sd += rand.nextFloat() * 20.0;
				seq += rand.nextInt(10);
			}
			testList(ctx, stopTimes);
		}
	}

	private GtfsStopTime stopTime(String tripId, int seq, String stopId,
			Integer dropoff, Integer arr, Integer pickup, Integer dep,
			Double shapeDist, String headsign) {
		return stopTime(tripId, seq, stopId, dropoff,
				arr == null ? null : GtfsLogicalTime.getTime(arr), pickup,
				dep == null ? null : GtfsLogicalTime.getTime(dep), shapeDist,
				headsign);
	}

	private GtfsStopTime stopTime(String tripId, int seq, String stopId,
			Integer dropoff, String arr, Integer pickup, String dep,
			Double shapeDist, String headsign) {
		try {
			return stopTime(tripId, seq, stopId, dropoff,
					arr == null ? null : GtfsLogicalTime.parseFromHH_MM_SS(arr),
					pickup,
					dep == null ? null : GtfsLogicalTime.parseFromHH_MM_SS(dep),
					shapeDist, headsign);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	private GtfsStopTime stopTime(String tripId, int seq, String stopId,
			Integer dropoff, GtfsLogicalTime arr, Integer pickup,
			GtfsLogicalTime dep, Double shapeDist, String headsign) {
		GtfsStopTime.Builder builder = new SimpleGtfsStopTime.Builder()
				.withTripId(GtfsTrip.id(tripId));
		builder.withStopSequence(GtfsTripStopSequence.fromSequence(seq));
		builder.withStopId(GtfsStop.id(stopId));
		builder.withArrivalTime(arr);
		builder.withDepartureTime(dep);
		if (dropoff != null)
			builder.withDropoffType(GtfsDropoffType.fromValue(dropoff));
		if (pickup != null)
			builder.withPickupType(GtfsPickupType.fromValue(pickup));
		builder.withShapeDistTraveled(shapeDist);
		builder.withStopHeadsign(headsign);
		return builder.build();
	}

	private void testList(PackedStopTimes.Context context,
			List<GtfsStopTime> stopTimes1) {
		GtfsTrip.Id tripId = stopTimes1.get(0).getTripId();
		PackedStopTimes pst = new PackedStopTimes(context, stopTimes1);
		List<GtfsStopTime> stopTimes2 = pst.getStopTimes(tripId, context);
		assertStopTimes(stopTimes1, stopTimes2);
	}

	public static void assertStopTimes(List<GtfsStopTime> stopTimes1,
			List<GtfsStopTime> stopTimes2) {
		assertEquals(stopTimes1.size(), stopTimes2.size());
		for (int i = 0; i < stopTimes1.size(); i++) {
			GtfsStopTime st1 = stopTimes1.get(i);
			GtfsStopTime st2 = stopTimes2.get(i);
			assertEquals(st1.getStopId(), st2.getStopId());
			assertEquals(st1.getStopSequence(), st2.getStopSequence());
			assertEquals(st1.getDropoffType(), st2.getDropoffType());
			assertEquals(st1.getPickupType(), st2.getPickupType());
			assertEquals(st1.getTimepoint(), st2.getTimepoint());
			Double sh1 = st1.getShapeDistTraveled();
			Double sh2 = st2.getShapeDistTraveled();
			assertTrue((sh1 == null && sh2 == null)
					|| (sh1 != null && sh2 != null));
			if (sh1 != null && sh2 != null) {
				/*
				 * Allow some slack in shape dist traveled as encoding /
				 * decoding floats will create some rounding errors.
				 */
				assertEquals(sh1, sh2, 1e-3);
			}
			assertEquals(st1.getDepartureTime(), st2.getDepartureTime());
			assertEquals(st1.getArrivalTime(), st2.getArrivalTime());
		}
	}

	/* This test does not really test anything, it just test my memory :) */
	@Test
	public void testByteEncoding() throws ParseException {
		for (int i = 0; i < 0xFFFF; i++) {
			byte[] data = new byte[2];
			int si = 0;
			data[si++] = (byte) ((i >> 8) & 0xFF);
			data[si++] = (byte) (i & 0xFF);
			si = 0;
			int j = ((data[si++] & 0xFF) << 8) | (data[si++] & 0xFF);
			assertEquals(i, j);
		}
	}

}
