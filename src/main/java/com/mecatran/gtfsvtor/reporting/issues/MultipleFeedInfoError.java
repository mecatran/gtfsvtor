package com.mecatran.gtfsvtor.reporting.issues;

import java.util.Arrays;
import java.util.List;

import com.mecatran.gtfsvtor.model.GtfsFeedInfo;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.SourceInfoWithFields;

@ReportIssuePolicy(severity = ReportIssueSeverity.ERROR, categoryName = "Multiple feed info")
public class MultipleFeedInfoError implements ReportIssue {

	private GtfsFeedInfo referenceFeedInfo, duplicatedFeedInfo;
	private List<SourceInfoWithFields> sourceInfos;

	public MultipleFeedInfoError(GtfsFeedInfo refFeedInfo,
			GtfsFeedInfo dupFeedInfo) {
		referenceFeedInfo = refFeedInfo;
		duplicatedFeedInfo = dupFeedInfo;
		this.sourceInfos = Arrays.asList(
				new SourceInfoWithFields(referenceFeedInfo.getSourceInfo()),
				new SourceInfoWithFields(duplicatedFeedInfo.getSourceInfo()));
	}

	@Override
	public List<SourceInfoWithFields> getSourceInfos() {
		return sourceInfos;
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text("Feed info should be present only once");
	}
}
