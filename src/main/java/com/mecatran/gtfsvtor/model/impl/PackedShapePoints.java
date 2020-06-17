package com.mecatran.gtfsvtor.model.impl;

import java.util.ArrayList;
import java.util.List;

import com.mecatran.gtfsvtor.model.GtfsShape;
import com.mecatran.gtfsvtor.model.GtfsShapePoint;
import com.mecatran.gtfsvtor.model.GtfsShapePointSequence;

public class PackedShapePoints {

	private static int E7_FACTOR = 10000000;

	public interface Context {
	}

	/*
	 * Since we only have a pointer, we could have saved off one object, but
	 * storing the data directly. One day maybe.
	 */
	private byte[] data;

	// TODO Remove
	private static final boolean _DEBUG = false;

	public PackedShapePoints(Context context,
			List<GtfsShapePoint> shapePoints) {
		if (shapePoints.isEmpty())
			throw new IllegalArgumentException(
					"Cannot pack an empty list of shape points.");

		int n = shapePoints.size();
		// Pre-allocate, being conservative
		byte[] tdata = new byte[n * (1 + 4 + 4 + 1 + 4 + 4) + 3];
		long lastLat = 0;
		long lastLon = 0;
		long lastLatDelta = 0;
		long lastLonDelta = 0;
		long lastSeq = 0;
		int ti = 0;

		// Encode shape point length
		if (n < 256) {
			// Rather common
			tdata[ti++] = (byte) (n);
		} else {
			// 0 is an invalid value for length
			tdata[ti++] = (byte) (0);
			tdata[ti++] = (byte) (n >> 8);
			tdata[ti++] = (byte) (n);
		}

		for (int i = 0; i < n; i++) {
			GtfsShapePoint shapePoint = shapePoints.get(i);

			// First byte lat/lon
			// b0-b3: lat flag
			// b4-b7: lon flag
			Double lat = shapePoint.getLat();
			Double lon = shapePoint.getLon();

			// Latitude
			int latFlag = 0;
			int latBytes = 0;
			long latVal = 0;
			if (lat == null) {
				latFlag = 0;
			} else {
				boolean delta2 = false;
				long ilat = (int) (lat * E7_FACTOR);
				long latDelta = ilat - lastLat;
				long latDelta2 = latDelta - lastLatDelta;
				if (Math.abs(latDelta) <= Math.abs(latDelta2)) {
					latVal = latDelta;
				} else {
					delta2 = true;
					latVal = latDelta2;
				}
				boolean mult10 = false;
				// Lots of values have 6 digits:
				// 10 multiplier, but only on 3 bytes
				if (latVal % 10 == 0 && latVal >= -83886080
						&& latVal < 83886080) {
					mult10 = true;
					latVal /= 10;
				}
				if (latVal >= -128 && latVal < 128) {
					latBytes = 1;
					latVal += 128;
				} else if (latVal >= -32768 && latVal < 32768) {
					latBytes = 2;
					latVal += 32768;
				} else if (latVal >= -8388608 && latVal < 8388608) {
					latBytes = 3;
					latVal += 8388608;
				} else if (latVal >= -2147483648L && latVal < 2147483648L) {
					latBytes = 4;
					latVal += 2147483648L;
				} else {
					// Can happen in rare case delta overflows a signed int,
					// eg when crossing the -180/+180 longitude line.
					latFlag = 1;
					latBytes = 4;
					latVal = ilat + 2147483648L;
				}
				if (latFlag != 1) {
					latFlag = delta2 ? 9 : 2;
					latFlag += mult10 ? 4 : 0;
					latFlag += latBytes - 1;
				}
				lastLat = ilat;
				lastLatDelta = latDelta;
			}

			// Longitude
			int lonFlag = 0;
			int lonBytes = 0;
			long lonVal = 0;
			if (lon == null) {
				lonFlag = 0;
			} else {
				boolean delta2 = false;
				long ilon = (int) (lon * E7_FACTOR);
				long lonDelta = ilon - lastLon;
				long lonDelta2 = lonDelta - lastLonDelta;
				if (Math.abs(lonDelta) <= Math.abs(lonDelta2)) {
					lonVal = lonDelta;
				} else {
					delta2 = true;
					lonVal = lonDelta2;
				}
				boolean mult10 = false;
				// Lots of values have 6 digits:
				// 10 multiplier, but only on 3 bytes
				if (lonVal % 10 == 0 && lonVal >= -83886080
						&& lonVal < 83886080) {
					mult10 = true;
					lonVal /= 10;
				}
				if (lonVal >= -128 && lonVal < 128) {
					lonBytes = 1;
					lonVal += 128;
				} else if (lonVal >= -32768 && lonVal < 32768) {
					lonBytes = 2;
					lonVal += 32768;
				} else if (lonVal >= -8388608 && lonVal < 8388608) {
					lonBytes = 3;
					lonVal += 8388608;
				} else if (lonVal >= -2147483648L && lonVal < 2147483648L) {
					lonBytes = 4;
					lonVal += 2147483648L;
				} else {
					// Can happen in rare case delta overflows a signed int,
					// eg when crossing the -180/+180 longitude line.
					lonFlag = 1;
					lonBytes = 4;
					lonVal = ilon + 2147483648L;
				}
				if (lonFlag != 1) {
					lonFlag = delta2 ? 9 : 2;
					lonFlag += mult10 ? 4 : 0;
					lonFlag += lonBytes - 1;
				}
				lastLon = ilon;
				lastLonDelta = lonDelta;
			}

			int firstByte = latFlag | (lonFlag << 4);
			if (_DEBUG) {
				System.out.println(String.format(
						"ENC %03d: First byte: %02x lat=%d (%d/%d) lon=%d (%d/%d)",
						i, firstByte, latFlag, latBytes, latVal, lonFlag,
						lonBytes, lonVal));
			}
			tdata[ti++] = (byte) (firstByte & 0xFF);
			switch (latBytes) {
			case 0:
				break;
			case 1:
				tdata[ti++] = (byte) (latVal & 0xFF);
				break;
			case 2:
				tdata[ti++] = (byte) ((latVal >> 8) & 0xFF);
				tdata[ti++] = (byte) (latVal & 0xFF);
				break;
			case 3:
				tdata[ti++] = (byte) ((latVal >> 16) & 0xFF);
				tdata[ti++] = (byte) ((latVal >> 8) & 0xFF);
				tdata[ti++] = (byte) (latVal & 0xFF);
				break;
			case 4:
				tdata[ti++] = (byte) ((latVal >> 24) & 0xFF);
				tdata[ti++] = (byte) ((latVal >> 16) & 0xFF);
				tdata[ti++] = (byte) ((latVal >> 8) & 0xFF);
				tdata[ti++] = (byte) (latVal & 0xFF);
				break;
			default:
				throw new RuntimeException("Invalid latBytes:" + latBytes);
			}
			switch (lonBytes) {
			case 0:
				break;
			case 1:
				tdata[ti++] = (byte) (lonVal & 0xFF);
				break;
			case 2:
				tdata[ti++] = (byte) ((lonVal >> 8) & 0xFF);
				tdata[ti++] = (byte) (lonVal & 0xFF);
				break;
			case 3:
				tdata[ti++] = (byte) ((lonVal >> 16) & 0xFF);
				tdata[ti++] = (byte) ((lonVal >> 8) & 0xFF);
				tdata[ti++] = (byte) (lonVal & 0xFF);
				break;
			case 4:
				tdata[ti++] = (byte) ((lonVal >> 24) & 0xFF);
				tdata[ti++] = (byte) ((lonVal >> 16) & 0xFF);
				tdata[ti++] = (byte) ((lonVal >> 8) & 0xFF);
				tdata[ti++] = (byte) (lonVal & 0xFF);
				break;
			default:
				throw new RuntimeException("Invalid lonBytes:" + lonBytes);
			}

			// Second byte - seq + shape dist
			// b0-b3 - seq flag
			// b4 - shape dist present
			GtfsShapePointSequence seq = shapePoint.getPointSequence();

			// Sequence
			int seqFlag = 0;
			int seqBytes = 0;
			long seqVal = 0;
			if (seq == null) {
				seqFlag = 0;
			} else {
				int iseq = seq.getSequence();
				// Note: by construction seqDelta cannot be negative
				long seqDelta = iseq - lastSeq;
				if (seqDelta == 1) {
					// Very common
					seqFlag = 1;
				} else if (seqDelta == 0) {
					// Rare, but we have room to spare ;)
					seqFlag = 2;
				} else if (seqDelta > 0 && seqDelta < 256) {
					// 1 byte
					seqFlag = 3;
					seqBytes = 1;
					seqVal = seqDelta;
				} else if (seqDelta > 0 && seqDelta < 65536) {
					// 2 bytes
					seqFlag = 4;
					seqBytes = 2;
					seqVal = seqDelta;
				} else if (seqDelta > 0 && seqDelta < 16777216) {
					// 3 bytes
					seqFlag = 5;
					seqBytes = 3;
					seqVal = seqDelta;
				} else {
					// 4 bytes
					// Here we could have negative values
					// if the first seq number is negative
					seqFlag = 6;
					seqBytes = 4;
					seqVal = seqDelta + 2147483648L;
				}
				lastSeq = iseq;
			}

			// Shape dist present
			Double sdt = shapePoint.getShapeDistTraveled();
			int sdtFlag = sdt == null ? 0 : 1;

			int secondByte = seqFlag | (sdtFlag << 4);
			if (_DEBUG) {
				System.out.println(String.format(
						"ENC %03d: Second byte: %02x seq=%d (%d/%d) sdt=%d", i,
						secondByte, seqFlag, seqBytes, seqVal, sdtFlag));
			}
			tdata[ti++] = (byte) (secondByte & 0xFF);
			switch (seqBytes) {
			case 0:
				break;
			case 1:
				tdata[ti++] = (byte) (seqVal & 0xFF);
				break;
			case 2:
				tdata[ti++] = (byte) ((seqVal >> 8) & 0xFF);
				tdata[ti++] = (byte) (seqVal & 0xFF);
				break;
			case 3:
				tdata[ti++] = (byte) ((seqVal >> 16) & 0xFF);
				tdata[ti++] = (byte) ((seqVal >> 8) & 0xFF);
				tdata[ti++] = (byte) (seqVal & 0xFF);
				break;
			case 4:
				tdata[ti++] = (byte) ((seqVal >> 24) & 0xFF);
				tdata[ti++] = (byte) ((seqVal >> 16) & 0xFF);
				tdata[ti++] = (byte) ((seqVal >> 8) & 0xFF);
				tdata[ti++] = (byte) (seqVal & 0xFF);
				break;
			default:
				throw new RuntimeException("Invalid seqBytes:" + seqBytes);
			}
			if (sdt != null) {
				int rawBits = Float.floatToIntBits(sdt.floatValue());
				tdata[ti++] = (byte) ((rawBits >> 24) & 0xFF);
				tdata[ti++] = (byte) ((rawBits >> 16) & 0xFF);
				tdata[ti++] = (byte) ((rawBits >> 8) & 0xFF);
				tdata[ti++] = (byte) (rawBits & 0xFF);
			}
		}

		this.data = new byte[ti];
		System.arraycopy(tdata, 0, this.data, 0, this.data.length);
	}

	public List<GtfsShapePoint> getShapePoints(GtfsShape.Id shapeId,
			Context context) {

		int ti = 0;
		int n = (data[ti++] & 0xFF);
		if (n == 0) {
			n = ((data[ti++] & 0xFF) << 8) | (data[ti++] & 0xFF);
		}
		List<GtfsShapePoint> shapePoints = new ArrayList<>(n);

		long ilat = 0;
		long ilatDelta = 0;
		long ilon = 0;
		long ilonDelta = 0;
		int iseq = 0;

		for (int i = 0; i < n; i++) {
			GtfsShapePoint.Builder builder = new SimpleGtfsShapePoint.Builder()
					.withShapeId(shapeId);

			int firstByte = (data[ti++] & 0xFF);
			int latFlag = (firstByte & 0b1111);
			int lonFlag = (firstByte >>> 4) & 0b1111;
			if (_DEBUG) {
				System.out.println(String.format(
						"DEC %03d: First byte: %02x lat=%d lon=%d  ilat=%d/%d ilon=%d/%d",
						i, firstByte, latFlag, lonFlag, ilat, ilatDelta, ilon,
						ilonDelta));
			}
			Double lat = null;
			Double lon = null;

			// Latitude
			long latVal = 0;
			int latBytes = 0;
			long latOff = 0;
			int latMult = 1;
			int latDelta2 = 0;
			switch (latFlag) {
			case 0:
				// Null
				break;
			case 1:
				latDelta2 = 0;
				latBytes = 4;
				latOff = -2147483648L;
				break;
			case 2:
				latDelta2 = 1;
				latBytes = 1;
				latOff = -128;
				break;
			case 3:
				latDelta2 = 1;
				latBytes = 2;
				latOff = -32768;
				break;
			case 4:
				latDelta2 = 1;
				latBytes = 3;
				latOff = -8388608;
				break;
			case 5:
				latDelta2 = 1;
				latBytes = 4;
				latOff = -2147483648L;
				break;
			case 6:
				latDelta2 = 1;
				latMult = 10;
				latBytes = 1;
				latOff = -128;
				break;
			case 7:
				latDelta2 = 1;
				latMult = 10;
				latBytes = 2;
				latOff = -32768;
				break;
			case 8:
				latDelta2 = 1;
				latMult = 10;
				latBytes = 3;
				latOff = -8388608;
				break;
			case 9:
				latDelta2 = 2;
				latBytes = 1;
				latOff = -128;
				break;
			case 10:
				latDelta2 = 2;
				latBytes = 2;
				latOff = -32768;
				break;
			case 11:
				latDelta2 = 2;
				latBytes = 3;
				latOff = -8388608;
				break;
			case 12:
				latDelta2 = 2;
				latBytes = 4;
				latOff = -2147483648L;
				break;
			case 13:
				latDelta2 = 2;
				latMult = 10;
				latBytes = 1;
				latOff = -128;
				break;
			case 14:
				latDelta2 = 2;
				latMult = 10;
				latBytes = 2;
				latOff = -32768;
				break;
			case 15:
				latDelta2 = 2;
				latMult = 10;
				latBytes = 3;
				latOff = -8388608;
				break;
			default:
				throw new RuntimeException("Invalid latFlag: " + latFlag);
			}
			if (latFlag != 0) {
				switch (latBytes) {
				case 0:
					break;
				case 1:
					latVal = (data[ti++] & 0xFF);
					break;
				case 2:
					latVal = ((data[ti++] & 0xFF) << 8) | (data[ti++] & 0xFF);
					break;
				case 3:
					latVal = ((data[ti++] & 0xFF) << 16)
							| ((data[ti++] & 0xFF) << 8) | (data[ti++] & 0xFF);
					break;
				case 4:
					latVal = ((long) (data[ti++] & 0xFF) << 24)
							| ((data[ti++] & 0xFF) << 16)
							| ((data[ti++] & 0xFF) << 8) | (data[ti++] & 0xFF);
					break;
				default:
					throw new RuntimeException("Invalid latBytes: " + latBytes);
				}
				latVal += latOff;
				latVal *= latMult;
				switch (latDelta2) {
				case 0:
					// Stored value itself
					ilatDelta = 0;
					ilat = latVal;
					break;
				case 1:
					// Stored the delta
					ilatDelta = latVal;
					ilat += ilatDelta;
					break;
				case 2:
					// Stored the delta of delta
					ilatDelta += latVal;
					ilat += ilatDelta;
					break;
				}
				if (latFlag != 0) {
					lat = ilat * 1. / E7_FACTOR;
				}
			}

			// Longitude
			long lonVal = 0;
			int lonBytes = 0;
			long lonOff = 0;
			int lonMult = 1;
			int lonDelta2 = 0;
			switch (lonFlag) {
			case 0:
				// Null
				break;
			case 1:
				lonDelta2 = 0;
				lonBytes = 4;
				lonOff = -2147483648L;
				break;
			case 2:
				lonDelta2 = 1;
				lonBytes = 1;
				lonOff = -128;
				break;
			case 3:
				lonDelta2 = 1;
				lonBytes = 2;
				lonOff = -32768;
				break;
			case 4:
				lonDelta2 = 1;
				lonBytes = 3;
				lonOff = -8388608;
				break;
			case 5:
				lonDelta2 = 1;
				lonBytes = 4;
				lonOff = -2147483648L;
				break;
			case 6:
				lonDelta2 = 1;
				lonMult = 10;
				lonBytes = 1;
				lonOff = -128;
				break;
			case 7:
				lonDelta2 = 1;
				lonMult = 10;
				lonBytes = 2;
				lonOff = -32768;
				break;
			case 8:
				lonDelta2 = 1;
				lonMult = 10;
				lonBytes = 3;
				lonOff = -8388608;
				break;
			case 9:
				lonDelta2 = 2;
				lonBytes = 1;
				lonOff = -128;
				break;
			case 10:
				lonDelta2 = 2;
				lonBytes = 2;
				lonOff = -32768;
				break;
			case 11:
				lonDelta2 = 2;
				lonBytes = 3;
				lonOff = -8388608;
				break;
			case 12:
				lonDelta2 = 2;
				lonBytes = 4;
				lonOff = -2147483648L;
				break;
			case 13:
				lonDelta2 = 2;
				lonMult = 10;
				lonBytes = 1;
				lonOff = -128;
				break;
			case 14:
				lonDelta2 = 2;
				lonMult = 10;
				lonBytes = 2;
				lonOff = -32768;
				break;
			case 15:
				lonDelta2 = 2;
				lonMult = 10;
				lonBytes = 3;
				lonOff = -8388608;
				break;
			default:
				throw new RuntimeException("Invalid lonFlag: " + lonFlag);
			}
			if (lonFlag != 0) {
				switch (lonBytes) {
				case 0:
					break;
				case 1:
					lonVal = (data[ti++] & 0xFF);
					break;
				case 2:
					lonVal = ((data[ti++] & 0xFF) << 8) | (data[ti++] & 0xFF);
					break;
				case 3:
					lonVal = ((data[ti++] & 0xFF) << 16)
							| ((data[ti++] & 0xFF) << 8) | (data[ti++] & 0xFF);
					break;
				case 4:
					lonVal = ((long) (data[ti++] & 0xFF) << 24)
							| ((data[ti++] & 0xFF) << 16)
							| ((data[ti++] & 0xFF) << 8) | (data[ti++] & 0xFF);
					break;
				default:
					throw new RuntimeException("Invalid lonBytes: " + lonBytes);
				}
				lonVal += lonOff;
				lonVal *= lonMult;
				switch (lonDelta2) {
				case 0:
					// Stored value itself
					ilonDelta = 0;
					ilon = lonVal;
					break;
				case 1:
					// Stored the delta
					ilonDelta = lonVal;
					ilon += ilonDelta;
					break;
				case 2:
					// Stored the delta of delta
					ilonDelta += lonVal;
					ilon += ilonDelta;
					break;
				}
				if (lonFlag != 0) {
					lon = ilon * 1. / E7_FACTOR;
				}
			}
			builder.withCoordinates(lat, lon);

			int secondByte = (data[ti++] & 0xFF);
			int seqFlag = (secondByte & 0b1111);
			int sdtFlag = (secondByte >>> 4) & 0b1;
			if (_DEBUG) {
				System.out.println(String.format(
						"DEC %03d: Second byte: %02x seq=%d sdt=%d  iseq=%d", i,
						secondByte, seqFlag, sdtFlag, iseq));
			}
			int seqBytes = 0;
			long seqOff = 0;
			long seqVal = 0;
			if (seqFlag != 0) {
				switch (seqFlag) {
				case 1:
					seqVal = 1;
					break;
				case 2:
					// 0, already set
					break;
				case 3:
					seqBytes = 1;
					break;
				case 4:
					seqBytes = 2;
					break;
				case 5:
					seqBytes = 3;
					break;
				case 6:
					seqBytes = 4;
					seqOff = -2147483648L;
					break;
				default:
					throw new RuntimeException("Invalid seqFlag: " + seqFlag);
				}
				switch (seqBytes) {
				case 0:
					break;
				case 1:
					seqVal = (data[ti++] & 0xFF);
					break;
				case 2:
					seqVal = ((data[ti++] & 0xFF) << 8) | (data[ti++] & 0xFF);
					break;
				case 3:
					seqVal = ((data[ti++] & 0xFF) << 16)
							| ((data[ti++] & 0xFF) << 8) | (data[ti++] & 0xFF);
					break;
				case 4:
					seqVal = ((long) (data[ti++] & 0xFF) << 24)
							| ((data[ti++] & 0xFF) << 16)
							| ((data[ti++] & 0xFF) << 8) | (data[ti++] & 0xFF);
					break;
				default:
					throw new RuntimeException("Invalid lonBytes: " + lonBytes);
				}
				seqVal += seqOff;
				iseq += seqVal;

				builder.withPointSequence(
						GtfsShapePointSequence.fromSequence(iseq));
			}

			if (sdtFlag == 1) {
				int sdtRaw = ((data[ti++] & 0xFF) << 24)
						| ((data[ti++] & 0xFF) << 16)
						| ((data[ti++] & 0xFF) << 8) | (data[ti++] & 0xFF);
				float sdt = Float.intBitsToFloat(sdtRaw);
				builder.withShapeDistTraveled((double) sdt);
			}

			// Build and add shape point
			GtfsShapePoint shapePoint = builder.build();
			shapePoints.add(shapePoint);
		}

		return shapePoints;
	}

	public int getDataSize() {
		return data.length;
	}
}
