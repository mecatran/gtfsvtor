package com.mecatran.gtfsvtor.reporting;

public class FormattingOptions {

	public enum SpeedUnit {
		MPS, KPH, MPH
	}

	private SpeedUnit speedUnit = SpeedUnit.MPS;

	public FormattingOptions() {
	}

	public FormattingOptions(SpeedUnit speedUnit) {
		this.speedUnit = speedUnit;
	}

	public SpeedUnit getSpeedUnit() {
		return speedUnit;
	}
}
