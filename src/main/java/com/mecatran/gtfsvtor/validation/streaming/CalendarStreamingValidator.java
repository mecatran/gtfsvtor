package com.mecatran.gtfsvtor.validation.streaming;

import static com.mecatran.gtfsvtor.validation.impl.StreamingValidationUtils.checkFieldValue;

import com.mecatran.gtfsvtor.model.GtfsCalendar;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.InvalidFieldValueIssue;
import com.mecatran.gtfsvtor.validation.ConfigurableOption;
import com.mecatran.gtfsvtor.validation.StreamingValidateType;
import com.mecatran.gtfsvtor.validation.StreamingValidator;

@StreamingValidateType(GtfsCalendar.class)
public class CalendarStreamingValidator
		implements StreamingValidator<GtfsCalendar> {

	@ConfigurableOption(description = "Year in the past before which an error is generated")
	private int minYearInThePast = 1900;

	@ConfigurableOption(description = "Year in the future after which an error is generated")
	private int maxYearInTheFuture = 2100;

	@Override
	public void validate(Class<? extends GtfsCalendar> clazz,
			GtfsCalendar calendar, StreamingValidator.Context context) {
		ReportSink reportSink = context.getReportSink();
		// Start date should be greater than end date
		if (calendar.getStartDate() != null && calendar.getEndDate() != null
				&& calendar.getStartDate()
						.compareTo(calendar.getEndDate()) > 0) {
			reportSink.report(new InvalidFieldValueIssue(context.getSourceRef(),
					calendar.getEndDate().toString(),
					"end date should be greater or equal than start date",
					"end_date"), context.getSourceInfo());
		}

		// Check date range
		checkFieldValue(
				date -> date != null && date.getYear() < minYearInThePast,
				calendar.getStartDate(), "start_date", context,
				"start date too far in the past");
		checkFieldValue(
				date -> date != null && date.getYear() < minYearInThePast,
				calendar.getEndDate(), "end_date", context,
				"end date too far in the past");
		checkFieldValue(
				date -> date != null && date.getYear() > maxYearInTheFuture,
				calendar.getStartDate(), "start_date", context,
				"start date too far in the future");
		checkFieldValue(
				date -> date != null && date.getYear() > maxYearInTheFuture,
				calendar.getEndDate(), "end_date", context,
				"end date too far in the future");
	}
}
