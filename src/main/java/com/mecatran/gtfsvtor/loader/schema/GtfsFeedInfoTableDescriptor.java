package com.mecatran.gtfsvtor.loader.schema;

import com.mecatran.gtfsvtor.loader.DataRowConverter;
import com.mecatran.gtfsvtor.model.GtfsFeedInfo;
import com.mecatran.gtfsvtor.model.GtfsObject;

@TableDescriptorPolicy(objectClass = GtfsFeedInfo.class, tableName = GtfsFeedInfo.TABLE_NAME, mandatory = false, mandatoryColumns = {
		"feed_publisher_name", "feed_publisher_url", "feed_lang" })
public class GtfsFeedInfoTableDescriptor implements GtfsTableDescriptor {

	@Override
	public GtfsObject<?> parseAndSave(DataRowConverter erow, Context context) {
		GtfsFeedInfo.Builder builder = new GtfsFeedInfo.Builder();

		builder.withSourceLineNumber(
				context.getSourceContext().getSourceRef().getLineNumber())
				.withFeedPublisherName(erow.getString("feed_publisher_name"))
				.withFeedPublisherUrl(erow.getString("feed_publisher_url"))
				.withFeedLang(erow.getLocale("feed_lang", true))
				.withDefaultLang(erow.getLocale("default_lang", false))
				.withFeedStartDate(
						erow.getLogicalDate("feed_start_date", false))
				.withFeedEndDate(erow.getLogicalDate("feed_end_date", false))
				.withFeedVersion(erow.getString("feed_version"))
				.withFeedContactEmail(erow.getString("feed_contact_email"))
				.withFeedContactUrl(erow.getString("feed_contact_url"));

		GtfsFeedInfo feedInfo = builder.build();
		context.getAppendableDao().setFeedInfo(feedInfo,
				context.getSourceContext());
		return feedInfo;
	}
}
