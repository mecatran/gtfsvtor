package com.mecatran.gtfsvtor.validation.streaming;

import static com.mecatran.gtfsvtor.validation.impl.StreamingValidationUtils.checkEmail;
import static com.mecatran.gtfsvtor.validation.impl.StreamingValidationUtils.checkLang;
import static com.mecatran.gtfsvtor.validation.impl.StreamingValidationUtils.checkUrl;

import com.mecatran.gtfsvtor.model.GtfsFeedInfo;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.InvalidFieldValueIssue;
import com.mecatran.gtfsvtor.validation.StreamingValidateType;
import com.mecatran.gtfsvtor.validation.StreamingValidator;

@StreamingValidateType(GtfsFeedInfo.class)
public class FeedInfoStreamingValidator
		implements StreamingValidator<GtfsFeedInfo> {

	@Override
	public void validate(Class<? extends GtfsFeedInfo> clazz,
			GtfsFeedInfo feedInfo, Context context) {
		ReportSink reportSink = context.getReportSink();
		checkUrl(feedInfo::getFeedPublisherUrl, "feed_publisher_url", context);
		if (feedInfo.getFeedLang() != null) {
			checkLang(() -> feedInfo.getFeedLang().getLanguage(), "feed_lang",
					context);
		}
		if (feedInfo.getDefaultLang() != null) {
			checkLang(() -> feedInfo.getDefaultLang().getLanguage(),
					"default_lang", context);
		}
		checkEmail(feedInfo::getFeedContactEmail, "feed_contact_email",
				context);
		checkUrl(feedInfo::getFeedContactUrl, "feed_contact_url", context);
		if (feedInfo.getFeedStartDate() != null
				&& feedInfo.getFeedEndDate() != null
				&& feedInfo.getFeedStartDate()
						.compareTo(feedInfo.getFeedEndDate()) > 0) {
			reportSink.report(new InvalidFieldValueIssue(
					feedInfo.getSourceInfo(),
					feedInfo.getFeedEndDate().toString(),
					"end date should be greater or equal than start date",
					"feed_end_date"));
		}
		if (feedInfo.getFeedStartDate() == null
				&& feedInfo.getFeedEndDate() != null) {
			reportSink.report(new InvalidFieldValueIssue(
					feedInfo.getSourceInfo(), null,
					"both feed_start_date and feed_end_date should be set, or none",
					"feed_start_date"));
		}
		if (feedInfo.getFeedEndDate() == null
				&& feedInfo.getFeedStartDate() != null) {
			reportSink.report(new InvalidFieldValueIssue(
					feedInfo.getSourceInfo(), null,
					"both feed_start_date and feed_end_date should be set, or none",
					"feed_end_date"));
		}
	}
}
