package com.mecatran.gtfsvtor.reporting.issues;

import java.util.Arrays;
import java.util.List;

import com.mecatran.gtfsvtor.model.GtfsFeedInfo;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.SourceRefWithFields;

@ReportIssuePolicy(severity = ReportIssueSeverity.ERROR, categoryName = "Multiple feed info")
public class MultipleFeedInfoError implements ReportIssue {

	private GtfsFeedInfo referenceFeedInfo, duplicatedFeedInfo;
	private List<SourceRefWithFields> sourceInfos;

	public MultipleFeedInfoError(GtfsFeedInfo refFeedInfo,
			GtfsFeedInfo dupFeedInfo) {
		referenceFeedInfo = refFeedInfo;
		duplicatedFeedInfo = dupFeedInfo;
		this.sourceInfos = Arrays.asList(
				new SourceRefWithFields(referenceFeedInfo.getSourceRef()),
				new SourceRefWithFields(duplicatedFeedInfo.getSourceRef()));
	}

	@Override
	public List<SourceRefWithFields> getSourceRefs() {
		return sourceInfos;
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text("Feed info should be present only once");
	}
}
