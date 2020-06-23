package com.mecatran.gtfsvtor.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mecatran.gtfsvtor.model.GtfsDropoffType;
import com.mecatran.gtfsvtor.model.GtfsLogicalTime;
import com.mecatran.gtfsvtor.model.GtfsPickupType;
import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.model.GtfsStopTime;
import com.mecatran.gtfsvtor.model.GtfsTimepoint;
import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.model.GtfsTripStopSequence;
import com.mecatran.gtfsvtor.model.impl.SimpleGtfsStopTime;
import com.mecatran.gtfsvtor.model.impl.SimpleGtfsStopTime.Builder;

/**
 * A list of stop times packed into two dedicated structures (times delta and
 * stop id and sequence). The packing algorithm rely on varying byte-length
 * delta encodings with bits packing.
 * 
 * The implementation rely on the caller to provide a full list of ordered stop
 * times at construction. Hence this implementation is not optimal in the case
 * we have stop times in random order.
 */
public class PackedStopTimes {

	public interface Context {

		public int indexStopId(GtfsStop.Id stopId);

		public GtfsStop.Id getStopIdIndex(int stopIdIndex);

		public PackedTimePattern intern(PackedTimePattern tData);

		public PackedStopPattern intern(PackedStopPattern sData);
	}

	private int baseTime;
	private PackedTimePattern timeData;
	private PackedStopPattern stopData;

	// TODO Remove
	private static final boolean _DEBUG = false;

	public PackedStopTimes(Context context, List<GtfsStopTime> stopTimes) {
		if (stopTimes.isEmpty())
			throw new IllegalArgumentException("Cannot encode an empty list");

		int n = stopTimes.size();
		// Pre-allocate, being conservative
		byte[] tdata = new byte[n * (1 + 3 + 3)];
		byte[] sdata = new byte[n * (1 + 1 + 3 + 4 + 2 + 4) + 3];
		Map<String, Integer> stopHeadsignIndexes = new HashMap<>();
		List<String> headsigns = new ArrayList<>();
		baseTime = Integer.MAX_VALUE; // ie, null
		int lastTime = 0;
		int lastStopIndex = 0;
		int lastStopSeq = 0;
		int lastHeadsignIndex = Integer.MAX_VALUE; // ie, null
		int ti = 0, si = 0;

		// Encode stop time length as unsigned short in sdata only
		if (n < 256) {
			// Very common, 255 is a lot of stops
			sdata[si++] = (byte) (n);
		} else {
			// 0 is an invalid value for length
			sdata[si++] = (byte) (0);
			sdata[si++] = (byte) (n >> 8);
			sdata[si++] = (byte) (n);
		}

		if (_DEBUG) {
			System.out.println(String.format("Packing: n=%d: %02x %02x", n,
					tdata[0], tdata[1]));
		}

		for (int i = 0; i < n; i++) {
			GtfsStopTime stopTime = stopTimes.get(i);

			// 1. Encode tdata (departure and arrival time deltas)

			// First byte
			// b0-b3: arrival time delta flag (0..15)
			// b4-b6: departure time delta flag (0..7)
			// b7: unused

			// Arrival time
			GtfsLogicalTime arv = stopTime.getArrivalTime();
			int arvFlag;
			int arvBytes = 0;
			int arvVal = 0;
			if (arv == null) {
				// Arrival is null, very common
				arvFlag = 0;
			} else {
				int arvSec = arv.getSecondSinceMidnight();
				if (baseTime == Integer.MAX_VALUE) {
					baseTime = arvSec;
					lastTime = arvSec;
				}
				int arvDelta = arvSec - lastTime;
				if (_DEBUG) {
					System.out.println(
							String.format("arvSec=%d lastTime=%d arvDelta=%d",
									arvSec, lastTime, arvDelta));
				}
				lastTime = arvSec;
				// Testing order is important
				if (arvDelta == 0) {
					// Delta is 0, rather common
					arvFlag = 1;
				} else if (arvDelta < 0) {
					// Delta is negative, do not optimize much
					// Hopefully time travels are rare nowadays
					arvFlag = 2;
					arvVal = -arvDelta;
					arvBytes = 3;
				} else if (arvDelta % 60 == 0) {
					// Delta is strictly positive and a round minute
					int arvDeltaMin = arvDelta / 60;
					if (arvDeltaMin < 9) {
						// [1..9[
						arvFlag = 2 + arvDeltaMin;
					} else if (arvDeltaMin < 256 + 9) {
						// [10..256+9[ - 1 unsigned byte
						arvFlag = 11;
						arvVal = arvDeltaMin - 9;
						arvBytes = 1;
					} else {
						// [256+20.. - 2 unsigned bytes
						arvFlag = 12;
						arvVal = arvDeltaMin - (256 + 9);
						arvBytes = 2;
					}
				} else {
					// Delta is strictly positive, any second
					if (arvDelta < 256 + 1) {
						// [1..256+1[ - 1 unsigned byte
						arvFlag = 13;
						arvVal = arvDelta - 1;
						arvBytes = 1;
					} else if (arvDelta < 65536 + 256 + 1) {
						// [256+1..65536+256+1[ - 2 unsigned bytes
						arvFlag = 14;
						arvVal = arvDelta - (256 + 1);
						arvBytes = 2;
					} else {
						// [65536+256+1.. - 3 unsigned bytes
						arvFlag = 15;
						arvVal = arvDelta - (65536 + 256 + 1);
						arvBytes = 3;
					}
				}
			}
			GtfsLogicalTime dpt = stopTime.getDepartureTime();
			int dptFlag;
			int dptBytes = 0;
			int dptVal = 0;
			if (dpt == null) {
				// Departure is null, very common
				dptFlag = 0;
			} else {
				int dptSec = dpt.getSecondSinceMidnight();
				if (baseTime == Integer.MAX_VALUE) {
					baseTime = dptSec;
					lastTime = dptSec;
				}
				int dptDelta = dptSec - lastTime;
				if (_DEBUG) {
					System.out.println(
							String.format("dptSec=%d lastTime=%d dptDelta=%d",
									dptSec, lastTime, dptDelta));
				}
				lastTime = dptSec;
				// Testing order is important
				if (dptDelta == 0) {
					// Delta is 0, rather common
					dptFlag = 1;
				} else if (dptDelta < 0) {
					// Delta is negative, do not optimize much
					// Hopefully time travels are rare nowadays
					dptFlag = 2;
					dptVal = -dptDelta;
					dptBytes = 3;
				} else if (dptDelta % 60 == 0) {
					// Delta is strictly positive and a round minute
					int dptDeltaMin = dptDelta / 60;
					if (dptDeltaMin < 256 + 1) {
						// [1..256+1[ - 1 unsigned byte
						dptFlag = 3;
						dptVal = dptDeltaMin - 1;
						dptBytes = 1;
					} else {
						// [256+1.. - 2 unsigned bytes
						dptFlag = 4;
						dptVal = dptDeltaMin - (256 + 1);
						dptBytes = 2;
					}
				} else {
					// Delta is strictly positive, any second
					if (dptDelta < 256 + 1) {
						// [1..256+1[ - 1 unsigned byte
						dptFlag = 5;
						dptVal = dptDelta - 1;
						dptBytes = 1;
					} else if (dptDelta < 65536 + 256 + 1) {
						// [256+1..65536+256+1[ - 2 unsigned bytes
						dptFlag = 6;
						dptVal = dptDelta - (256 + 1);
						dptBytes = 2;
					} else {
						// [65536+256+1.. - 3 unsigned bytes
						dptFlag = 7;
						dptVal = dptDelta - (65536 + 256 + 1);
						dptBytes = 3;
					}
				}
			}
			int firstTByte = arvFlag | (dptFlag << 4);
			if (_DEBUG) {
				System.out.println(String.format(
						"%03d: First T byte: %02x arv=%d (%d/%d) dpt=%d (%d/%d)",
						i, firstTByte, arvFlag, arvBytes, arvVal, dptFlag,
						dptBytes, dptVal));
			}
			tdata[ti++] = (byte) (firstTByte & 0xFF);
			switch (arvBytes) {
			case 0:
				break;
			case 1:
				tdata[ti++] = (byte) (arvVal & 0xFF);
				break;
			case 2:
				tdata[ti++] = (byte) ((arvVal >> 8) & 0xFF);
				tdata[ti++] = (byte) (arvVal & 0xFF);
				break;
			case 3:
				tdata[ti++] = (byte) ((arvVal >> 16) & 0xFF);
				tdata[ti++] = (byte) ((arvVal >> 8) & 0xFF);
				tdata[ti++] = (byte) (arvVal & 0xFF);
				break;
			default:
				throw new RuntimeException("Invalid arvBytes:" + arvBytes);
			}
			switch (dptBytes) {
			case 0:
				break;
			case 1:
				tdata[ti++] = (byte) (dptVal & 0xFF);
				break;
			case 2:
				tdata[ti++] = (byte) ((dptVal >> 8) & 0xFF);
				tdata[ti++] = (byte) (dptVal & 0xFF);
				break;
			case 3:
				tdata[ti++] = (byte) ((dptVal >> 16) & 0xFF);
				tdata[ti++] = (byte) ((dptVal >> 8) & 0xFF);
				tdata[ti++] = (byte) (dptVal & 0xFF);
				break;
			default:
				throw new RuntimeException("Invalid dptBytes:" + dptBytes);
			}

			// 2. Encode sdata
			// (pickup/dropoff/timepoint/stopID/seq/headsign/shapedist)

			// First byte - pickup/dropoff/timepoint/shapedist
			// b0-b2: dropoff
			// b3-b5: pickup
			// b6-b7: timepoint
			int firstSByte = 0;
			GtfsDropoffType dropoff = stopTime.getDropoffType().orElse(null);
			firstSByte |= dropoff == null ? 0b111 : dropoff.getValue();
			GtfsPickupType pickup = stopTime.getPickupType().orElse(null);
			firstSByte |= (pickup == null ? 0b111 : pickup.getValue()) << 3;
			GtfsTimepoint timepoint = stopTime.getTimepoint().orElse(null);
			firstSByte |= (timepoint == null ? 0b11
					: timepoint.getValue()) << 6;
			sdata[si++] = (byte) (firstSByte & 0xFF);

			if (_DEBUG) {
				System.out.println(String.format("%03d: First S byte: %02x", i,
						firstSByte));
			}

			// Second byte - stop ID index/seq delta/headsign index
			// b0-b2: stop index delta flag (0..7)
			// b3-b4: stop sequence delta flag (0..3)
			// b5-b6: stop headsign index flag (0..3)
			// b7: shape dist flag

			// Stop index delta flag
			GtfsStop.Id stopId = stopTime.getStopId();
			int stopFlag;
			int stopBytes = 0;
			int stopVal = 0;
			if (stopId == null) {
				stopFlag = 0;
			} else {
				int stopIndex = context.indexStopId(stopTime.getStopId());
				int siDelta = stopIndex - lastStopIndex;
				lastStopIndex = stopIndex;
				if (siDelta == 0) {
					stopFlag = 1;
				} else if (siDelta == 1) {
					stopFlag = 2;
				} else if (siDelta == 2) {
					stopFlag = 3;
				} else if (siDelta == -1) {
					stopFlag = 4;
				} else if (siDelta >= -128 && siDelta < 128) {
					// 1 byte
					stopFlag = 5;
					stopVal = siDelta + 128;
					stopBytes = 1;
				} else if (siDelta >= -32768 && siDelta < 32768) {
					// 2 bytes
					stopFlag = 6;
					stopVal = siDelta + 32768;
					stopBytes = 2;
				} else {
					stopFlag = 7;
					stopVal = siDelta + 8388608;
					stopBytes = 3;
				}
			}
			// Stop seq flag
			int seqFlag;
			int seqBytes = 0;
			int seqVal = 0;
			// stopSeq is guaranteed to be not null
			int stopSeq = stopTime.getStopSequence().getSequence();
			int seqDelta = stopSeq - lastStopSeq;
			lastStopSeq = stopSeq;
			if (seqDelta == 1) {
				// [1]
				// This should be very common
				seqFlag = 0;
			} else if (seqDelta >= 0 && seqDelta < 256) {
				// [0..256[
				// Rarer, but not uncommon
				seqFlag = 1;
				seqVal = seqDelta;
				seqBytes = 1;
			} else if (seqDelta >= 0 && seqDelta < 65536 + 256) {
				// [256..65536 + 256[
				seqFlag = 2;
				seqVal = seqDelta - 256;
				seqBytes = 2;
			} else {
				// Fallback for all values, including negative
				seqFlag = 3;
				seqVal = seqDelta;
				seqBytes = 4;
			}
			// Stop headsign flag
			int sthFlag;
			int sthBytes = 0;
			int sthVal = 0;
			String stopHeadsign = stopTime.getStopHeadsign();
			if (stopHeadsign == null) {
				// Very common
				sthFlag = 0;
			} else {
				Integer sthIndex = stopHeadsignIndexes.get(stopHeadsign);
				if (sthIndex == null) {
					sthIndex = headsigns.size();
					headsigns.add(stopHeadsign);
					stopHeadsignIndexes.put(stopHeadsign, sthIndex);
				}
				if (sthIndex == lastHeadsignIndex) {
					// Identical to previously used index
					sthFlag = 1;
				} else if (sthIndex < 256) {
					// [0..256[
					sthFlag = 2;
					sthVal = sthIndex;
					sthBytes = 1;
				} else {
					// I hope we will not have more than 65536+255 different
					// stop headsigns
					sthFlag = 3;
					sthVal = sthIndex - 256;
					sthBytes = 2;
				}
				lastHeadsignIndex = sthIndex;
			}
			// Shape dist traveled flag is present
			Double shapeDist = stopTime.getShapeDistTraveled();
			int sdtFlag = shapeDist == null ? 0 : 1;
			int secondSByte = stopFlag | (seqFlag << 3) | (sthFlag << 5)
					| (sdtFlag << 7);
			sdata[si++] = (byte) (secondSByte & 0xFF);

			if (_DEBUG) {
				System.out.println(String.format(
						"%03d: Second S byte: %02x stp=%d seq=%d sth=%d sdt=%d",
						i, secondSByte, stopFlag, seqFlag, sthFlag, sdtFlag));
			}

			switch (stopBytes) {
			case 0:
				break;
			case 1:
				sdata[si++] = (byte) (stopVal & 0xFF);
				break;
			case 2:
				sdata[si++] = (byte) ((stopVal >> 8) & 0xFF);
				sdata[si++] = (byte) (stopVal & 0xFF);
				break;
			case 3:
				sdata[si++] = (byte) ((stopVal >> 16) & 0xFF);
				sdata[si++] = (byte) ((stopVal >> 8) & 0xFF);
				sdata[si++] = (byte) (stopVal & 0xFF);
				break;
			default:
				throw new RuntimeException("Invalid stopBytes:" + stopBytes);
			}
			switch (seqBytes) {
			case 0:
				break;
			case 1:
				sdata[si++] = (byte) (seqVal & 0xFF);
				break;
			case 2:
				sdata[si++] = (byte) ((seqVal >> 8) & 0xFF);
				sdata[si++] = (byte) (seqVal & 0xFF);
				break;
			case 4:
				sdata[si++] = (byte) ((seqVal >> 24) & 0xFF);
				sdata[si++] = (byte) ((seqVal >> 16) & 0xFF);
				sdata[si++] = (byte) ((seqVal >> 8) & 0xFF);
				sdata[si++] = (byte) (seqVal & 0xFF);
				break;
			default:
				throw new RuntimeException("Invalid seqBytes:" + seqBytes);
			}
			switch (sthBytes) {
			case 0:
				break;
			case 1:
				sdata[si++] = (byte) (sthVal & 0xFF);
				break;
			case 2:
				sdata[si++] = (byte) ((sthVal >> 8) & 0xFF);
				sdata[si++] = (byte) (sthVal & 0xFF);
				break;
			default:
				throw new RuntimeException("Invalid sthBytes:" + sthBytes);
			}
			if (shapeDist != null) {
				int rawBits = Float.floatToIntBits(shapeDist.floatValue());
				sdata[si++] = (byte) ((rawBits >> 24) & 0xFF);
				sdata[si++] = (byte) ((rawBits >> 16) & 0xFF);
				sdata[si++] = (byte) ((rawBits >> 8) & 0xFF);
				sdata[si++] = (byte) (rawBits & 0xFF);
			}
		}

		byte[] tdata2 = new byte[ti];
		byte[] sdata2 = new byte[si];
		System.arraycopy(tdata, 0, tdata2, 0, tdata2.length);
		System.arraycopy(sdata, 0, sdata2, 0, sdata2.length);
		if (headsigns != null) {
			for (int i = 0; i < headsigns.size(); i++) {
				headsigns.set(i, headsigns.get(i).intern());
			}
		}
		this.timeData = context.intern(new PackedTimePattern(tdata2));
		this.stopData = context
				.intern(new PackedStopPattern(sdata2, headsigns));
	}

	public List<GtfsStopTime> getStopTimes(GtfsTrip.Id tripId,
			Context context) {

		byte[] tdata = timeData.getTData();
		byte[] sdata = stopData.getSData();
		List<String> headsigns = stopData.getHeadsigns();
		int ti = 0, si = 0;

		int n = (sdata[si++] & 0xFF);
		if (n == 0) {
			n = ((sdata[si++] & 0xFF) << 8) | (sdata[si++] & 0xFF);
		}

		List<GtfsStopTime> stopTimes = new ArrayList<>(n);
		int time = baseTime;
		int stopIndex = 0;
		int stopSeq = 0;
		int headsignIndex = Integer.MAX_VALUE;

		if (_DEBUG) {
			System.out.println(String.format("Unpacking: n=%d %02x %02x", n,
					tdata[0], tdata[1]));
		}

		for (int i = 0; i < n; i++) {
			GtfsStopTime.Builder builder = new SimpleGtfsStopTime.Builder()
					.withTripId(tripId);

			// Load T data (time deltas)
			int firstTByte = (tdata[ti++] & 0xFF);
			int arvFlag = (firstTByte & 0b1111);
			int dptFlag = (firstTByte >>> 4) & 0b111;
			if (_DEBUG) {
				System.out.println(
						String.format("%03d: First T byte: %02x arv=%d dpt=%d",
								i, firstTByte, arvFlag, dptFlag));
			}

			// Load arrival
			int arvBytes = 0;
			int arvOff = 0;
			int arvMult = 1;
			int arvVal = 0;
			switch (arvFlag) {
			case 0:
				// Null
				break;
			case 1:
				// Zero
				arvVal = 0;
				break;
			case 2:
				// Negative values
				arvBytes = 3;
				arvMult = -1;
				break;
			case 3:
			case 4:
			case 5:
			case 6:
			case 7:
			case 8:
			case 9:
			case 10:
				// Minutes constant
				arvVal = arvFlag;
				arvOff = -2;
				arvMult = 60;
				break;
			case 11:
				// Minutes on 1 byte
				arvBytes = 1;
				arvOff = 9;
				arvMult = 60;
				break;
			case 12:
				// Minutes on 2 bytes
				arvBytes = 2;
				arvOff = 256 + 9;
				arvMult = 60;
				break;
			case 13:
				// Seconds on 1 byte
				arvBytes = 1;
				arvOff = 1;
				break;
			case 14:
				// Seconds on 2 bytes
				arvBytes = 2;
				arvOff = 256 + 1;
				break;
			case 15:
				// Seconds on 3 bytes
				arvBytes = 3;
				arvOff = 65536 + 256 + 1;
				break;
			default:
				throw new RuntimeException("Invalid arvFlag: " + arvFlag);
			}
			switch (arvBytes) {
			case 0:
				break;
			case 1:
				arvVal = (tdata[ti++] & 0xFF);
				break;
			case 2:
				arvVal = ((tdata[ti++] & 0xFF) << 8) | (tdata[ti++] & 0xFF);
				break;
			case 3:
				arvVal = ((tdata[ti++] & 0xFF) << 16)
						| ((tdata[ti++] & 0xFF) << 8) | (tdata[ti++] & 0xFF);
				break;
			default:
				throw new RuntimeException("Invalid arvBytes: " + arvBytes);
			}
			arvVal += arvOff;
			arvVal *= arvMult;
			if (arvFlag != 0) {
				time += arvVal;
				builder.withArrivalTime(GtfsLogicalTime.getTime(time));
			}

			// Load departure
			int dptBytes = 0;
			int dptOff = 0;
			int dptMult = 1;
			int dptVal = 0;
			switch (dptFlag) {
			case 0:
				// Null
				break;
			case 1:
				// Zero
				break;
			case 2:
				// Negative values
				dptBytes = 3;
				dptMult = -1;
				break;
			case 3:
				// Minutes on 1 byte
				dptBytes = 1;
				dptOff = 1;
				dptMult = 60;
				break;
			case 4:
				// Minutes on 2 bytes
				dptBytes = 2;
				dptOff = 256 + 1;
				dptMult = 60;
				break;
			case 5:
				// Seconds on 1 byte
				dptBytes = 1;
				dptOff = 1;
				break;
			case 6:
				// Seconds on 2 bytes
				dptBytes = 2;
				dptOff = 256 + 1;
				break;
			case 7:
				// Seconds on 3 bytes
				dptBytes = 3;
				dptOff = 65536 + 256 + 1;
				break;
			default:
				throw new RuntimeException("Invalid dptFlag: " + dptFlag);
			}
			switch (dptBytes) {
			case 0:
				// Keep depVal as it is
				break;
			case 1:
				dptVal = (tdata[ti++] & 0xFF);
				break;
			case 2:
				dptVal = ((tdata[ti++] & 0xFF) << 8) | (tdata[ti++] & 0xFF);
				break;
			case 3:
				dptVal = ((tdata[ti++] & 0xFF) << 16)
						| ((tdata[ti++] & 0xFF) << 8) | (tdata[ti++] & 0xFF);
				break;
			default:
				throw new RuntimeException("Invalid dptBytes: " + dptBytes);
			}
			dptVal += dptOff;
			dptVal *= dptMult;
			if (dptFlag != 0) {
				time += dptVal;
				builder.withDepartureTime(GtfsLogicalTime.getTime(time));
			}

			// Load SDATA (stop pattern)

			// First byte - pickup/dropoff/timepoint/shapedist
			// b0-b2: dropoff
			// b3-b5: pickup
			// b6-b7: timepoint
			int firstSByte = sdata[si++] & 0xFF;
			int drpVal = (firstSByte & 0b111);
			int pckVal = (firstSByte >> 3) & 0b111;
			int tptVal = (firstSByte >> 6) & 0b11;
			if (_DEBUG) {
				System.out.println(String.format(
						"%3d: First S byte: %02x drp=%d pck=%d tpt=%d", i,
						firstSByte, drpVal, pckVal, tptVal));
			}
			if (drpVal != 0b111)
				builder.withDropoffType(GtfsDropoffType.fromValue(drpVal));
			if (pckVal != 0b111)
				builder.withPickupType(GtfsPickupType.fromValue(pckVal));
			if (tptVal != 0b11)
				builder.withTimepoint(GtfsTimepoint.fromValue(tptVal));

			// Second byte - stop ID index/seq delta/headsign index
			// b0-b2: stop index delta flag (0..7)
			// b3-b4: stop sequence delta flag (0..3)
			// b5-b6: stop headsign index flag (0..3)
			// b7: shape dist flag
			int secondSByte = sdata[si++] & 0xFF;
			int stopFlag = (secondSByte & 0b111);
			int seqFlag = (secondSByte >> 3) & 0b11;
			int sthFlag = (secondSByte >> 5) & 0b11;
			int sdtFlag = (secondSByte >> 7) & 0b1;

			// Stop flag
			int stopBytes = 0;
			int stopVal = 0;
			int stopOff = 0;
			switch (stopFlag) {
			case 0:
				// Null
				break;
			case 1:
				// 0
				break;
			case 2:
				// 1
				stopVal = 1;
				break;
			case 3:
				// 2
				stopVal = 2;
				break;
			case 4:
				// -1
				stopVal = -1;
				break;
			case 5:
				// 1 byte
				stopBytes = 1;
				stopOff = -128;
				break;
			case 6:
				// 2 bytes
				stopBytes = 2;
				stopOff = -32768;
				break;
			case 7:
				// 3 bytes
				stopBytes = 3;
				stopOff = -8388608;
				break;
			default:
				throw new RuntimeException("Invalid stopFlag: " + stopFlag);
			}
			switch (stopBytes) {
			case 0:
				// Keep stopVal as it is
				break;
			case 1:
				stopVal = (sdata[si++] & 0xFF);
				break;
			case 2:
				stopVal = ((sdata[si++] & 0xFF) << 8) | (sdata[si++] & 0xFF);
				break;
			case 3:
				stopVal = ((sdata[si++] & 0xFF) << 16)
						| ((sdata[si++] & 0xFF) << 8) | (sdata[si++] & 0xFF);
				break;
			default:
				throw new RuntimeException("Invalid stopBytes: " + stopBytes);
			}
			if (stopFlag == 0) {
				// Stop is null
			} else {
				stopVal += stopOff;
				stopIndex += stopVal;
				GtfsStop.Id stopId = context.getStopIdIndex(stopIndex);
				builder.withStopId(stopId);
			}

			// Seq flag
			int seqVal = 0;
			int seqBytes = 0;
			int seqOff = 0;
			switch (seqFlag) {
			case 0:
				seqVal = 1;
				break;
			case 1:
				seqBytes = 1;
				break;
			case 2:
				seqBytes = 2;
				seqOff = 256;
				break;
			case 3:
				seqBytes = 4;
				break;
			default:
				throw new RuntimeException("Invalid seqFlag: " + seqFlag);
			}
			switch (seqBytes) {
			case 0:
				// Keep seqVal as it is
				break;
			case 1:
				seqVal = (sdata[si++] & 0xFF);
				break;
			case 2:
				seqVal = ((sdata[si++] & 0xFF) << 8) | (sdata[si++] & 0xFF);
				break;
			case 4:
				seqVal = ((sdata[si++] & 0xFF) << 24)
						| ((sdata[si++] & 0xFF) << 16)
						| ((sdata[si++] & 0xFF) << 8) | (sdata[si++] & 0xFF);
				break;
			default:
				throw new RuntimeException("Invalid seqBytes: " + seqBytes);
			}
			seqVal += seqOff;
			stopSeq += seqVal;
			builder.withStopSequence(
					GtfsTripStopSequence.fromSequence(stopSeq));

			// Stop headsign
			int sthVal = 0;
			int sthBytes = 0;
			int sthOff = 0;
			switch (sthFlag) {
			case 0:
				// Null
				break;
			case 1:
				// Same as last
				sthVal = headsignIndex;
				break;
			case 2:
				// 1 byte
				sthBytes = 1;
				break;
			case 3:
				// 2 bytes
				sthBytes = 2;
				sthOff = 256;
				break;
			default:
				throw new RuntimeException("Invalid sthFlag: " + sthFlag);
			}
			switch (sthBytes) {
			case 0:
				break;
			case 1:
				sthVal = (sdata[si++] & 0xFF);
				break;
			case 2:
				sthVal = ((sdata[si++] & 0xFF) << 8) | (sdata[si++] & 0xFF);
				break;
			default:
				throw new RuntimeException("Invalid sthBytes: " + sthBytes);
			}
			sthVal += sthOff;
			if (sthFlag != 0) {
				headsignIndex = sthVal;
				builder.withStopHeadsign(headsigns.get(sthVal));
			}

			// Load shape dist
			if (sdtFlag == 1) {
				int sdtRaw = ((sdata[si++] & 0xFF) << 24)
						| ((sdata[si++] & 0xFF) << 16)
						| ((sdata[si++] & 0xFF) << 8) | (sdata[si++] & 0xFF);
				float sdt = Float.intBitsToFloat(sdtRaw);
				builder.withShapeDistTraveled((double) sdt);
			}

			// Build and add stop time
			GtfsStopTime stopTime = builder.build();
			stopTimes.add(stopTime);
		}

		return stopTimes;
	}
}
